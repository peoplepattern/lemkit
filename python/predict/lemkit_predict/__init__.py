# This is a dependentless script for using a trained linear model
# to make predictions. Function predict() takes two arguments:
#
# model = a trained linear model file in json format
#
# test_set = file with format:
# label feature:value feature:value feature:value
# If label is not known leave first item blank, but retain space
#
# outfile = file name of where predictions will be written
#

from struct import unpack
import sys
import io

# attempt to import fast c implementation, if fails go to slow backup
try:
    import mmh3 as mh
except:
    print>>sys.stderr, "mmh3 not installed, switching to slow Python implementation"
    import murmurh3 as mh

def read_int(r):
    return unpack('>i', r.read(4))[0]


def read_short(r):
    return unpack('>h', r.read(2))[0]


def read_string(r):
    length = read_int(r)
    return unpack(">%ds" % length, r.read(length))[0]


def read_double(r):
    return unpack('>d', r.read(8))[0]


def convert_list_to_dict(alist):
    adict = {}
    j = 0
    for i in alist:
        adict[i] = j
        j += 1
    return adict


def read_list(r):
    alist = []
    list_length = read_int(r)
    for i in range(1, list_length + 1):
        alist.append(read_string(r))
    return alist


def read_table(r):
    adict = {}
    table_length = read_int(r)
    for i in range(1, table_length + 1):
        s1 = read_string(r)
        r1 = read_int(r)
        adict[s1] = r1
    return adict


def readBinaryModel(filename):
    model = {}
    hashmod = None
    featureFeaturesID = None
    feature_index = None
    maxfeatsID = None
    hashmod = None
    nonzero_indices = None

    with io.open(filename, 'rb') as r:
        magicNumber = read_int(r)
        majorVersion = read_short(r)
        minorVersion = read_short(r)
        labelID = read_short(r)
        label_index = read_table(r)
        featureTypeID = read_short(r)
        feature_hashing = read_short(r)
        # IF 1 then should be exact features
        if feature_hashing == 1:
            featureFeaturesID = read_short(r)
            feature_index = convert_list_to_dict(read_list(r))
        elif feature_hashing == 2:
            maxfeatsID = read_short(r)
            hashmod = read_int(r)
        weightsTypeID = read_short(r)
        weightsType = read_short(r)
        if weightsType == 1:
            weightsValuesID = read_short(r)
            weight_length = read_int(r)
            weights = []
            for i in range(1, weight_length + 1):
                row_length = read_int(r)
                weights.append([read_double(r)
                                for i in range(1, row_length + 1)])
        elif weightsType == 2:
            weightsValuesID = read_short(r)
            weight_length = read_int(r)
            nonzero_indices_length = read_int(r)
            nonzero_indices = {}
            weights = []
            for i in range(1, nonzero_indices_length + 1):
                nonzero_indices[read_int(r)] = i - 1
                # nonzero_indices.append(read_int(r))
            for i in range(1, weight_length + 1):
                weights.append([read_double(r)
                                for j in range(1, nonzero_indices_length + 1)])

    return weights, label_index, feature_index, hashmod, nonzero_indices

def sum_weights(weights, label_index, feature_index, label, row):
    return sum([float(l.split(':')[1]) * 
            weights[label_index[label]][feature_index[l.split(':')[0]]]
            for 
            l
            in
            row
            if l.split(':')[0] in feature_index ]) 

def sum_weights_hashed(weights, label_index, row, label, mod):
    return sum([float(l.split(':')[1]) * 
            weights[label_index[label]][mh.hash(l.split(':')[0]) % mod]
            for 
            l
            in
            row])

def sum_weights_hashed_sparse(weights, label_index, row, label, mod, nonzero_indices):
    return sum([float(l.split(':')[1]) * 
            weights[label_index[label]][nonzero_indices[mh.hash(l.split(':')[0]) % mod]]
            for 
            l
            in
            row if (mh.hash(l.split(':')[0]) % mod) in nonzero_indices])

def exact_predict(weights, label_index, feature_index, test_set):
    predictions = []
    total = 0
    with io.open(test_set, 'rb') as t:
        for line in t:
            total += 1
            row = line.strip().split('|')[1].strip().split(' ')
            bestv = -99.99
            label_choice = "NONE"
            for label in label_index:
                v = (weights[label_index[label]][0] +
                     sum_weights(weights, label_index, feature_index, label, row))
                if v > bestv:
                    label_choice = label
                    bestv = v
            predictions.append([str(total), line.strip().split('|')[0].strip(), label_choice, bestv])
            #print predictions[-1]
    return predictions


def hashed_predict(weights, label_index, mod, test_set):
    predictions = []
    total = 0
    with io.open(test_set, 'rb') as t:
        for line in t:
            if len(line) > 0:
                total += 1
                #print type(line)
                row = line.strip().split('|')[1].strip().split(' ')
                #print type(row)
                bestv = -99.99
                label_choice = "NONE"
                for label in label_index:
                    v = (weights[label_index[label]][0] +
                         sum_weights_hashed(weights, label_index, row, label, mod))
                    if v > bestv:
                        label_choice = label
                        bestv = v
                predictions.append([str(total), line.strip().split('|')[0].strip(), label_choice, bestv])
    return predictions


def hashed_predict_sparse(weights, label_index, mod,
                          test_set, nonzero_indices):
    predictions = []
    total = 0
    with io.open(test_set, 'rb') as t:
        for line in t:
            if len(line) > 0:
                total += 1
                row = line.strip().split('|')[1].strip().split(' ')
                bestv = -99.99
                label_choice = "NONE"
                for label in label_index:
                    v = (weights[label_index[label]][0] +
                        sum_weights_hashed_sparse(weights, 
                                                  label_index,
                                                  row, 
                                                  label, 
                                                  mod, 
                                                  nonzero_indices))
                    if v > bestv:
                        label_choice = label
                        bestv = v
                predictions.append([str(total), line.strip().split('|')[0].strip(), label_choice, bestv])
    return predictions


def predict(model, test_set, modeltype="binary", outfile=None):

    # print "Loading Model @ ", model

    if modeltype == "json":
        import json
        with open(model, 'r') as r:
            model_json = json.load(r)
        if model_json['features']['type'] == 'exact':
            predictions = exact_predict(model_json['weights']['values'],
                                        model_json['labels'],
                                        model_json['features']['features'],
                                        test_set)
        elif model_json['features']['type'] == 'hashed':
            predictions = hashed_predict(model_json['weights']['values'],
                                         model_json['labels'],
                                         model_json['features']['maxfeats'],
                                         test_set)
    elif modeltype.lower() == "binary":
        weights, label_index, feature_index, mod, nonzero_indices = read_binary.readBinaryModel(
            model)
        if feature_index is not None:
            if nonzero_indices is not None:
                predictions = exact_predict_sparse(weights, label_index, feature_index,
                                                   test_set, nonzero_indices)
            else:
                predictions = exact_predict(
                    weights, label_index, feature_index, test_set)
        elif mod is not None:
            if nonzero_indices is not None:
                predictions = hashed_predict_sparse(weights, label_index, mod,
                                                    test_set, nonzero_indices)
            else:
                predictions = hashed_predict(
                    weights, label_index, mod, test_set)

    if outfile is not None:
        with io.open(outfile, 'w', encoding='utf-8') as w:
            for p in predictions:
                w.write(u' '.join(p[:-1]) + '\n')
        print "Writing predictions complete check them @ ", outfile

    return predictions