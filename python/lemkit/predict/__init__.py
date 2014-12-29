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
