import json
import io
import sys
from operator import itemgetter

def writeBinaryModel(filename, weights, feature_index, label_index,
					 hash_trick, hashmod, majorVersion, minorVersion,
					 sparse="False"):
	from struct import pack
	
	def write_int(w, int1):
		w.write(pack('>i', int1))

	def write_short(w, int1):
		w.write(pack('>h', int1))

	def write_string(w, str1):
		w.write(pack('>i%ds' % (len(str1.encode('utf-8')),), len(str1.encode('utf-8')) , str1.encode('utf-8')))

	def write_double(w, dub1):
		w.write(pack('>d', dub1))

	def write_list(w, feature_dict):
		write_int(w, len(feature_dict))
		for i in sorted(feature_dict.items(), key=itemgetter(1), reverse=False):
			write_string(w, i[0])

	def write_table(w, label_dict):
		write_int(w, len(label_dict))
		for i in label_dict.items():
			write_string(w, i[0])
			write_int(w, i[1])

	def get_nonzero_indices(weights):
		i = 0
		non_zeros = []
		for col in map(lambda *a: list(a), *weights):
			if len(set(col)) > 1:
				non_zeros.append(i)
			i += 1
		return non_zeros

	def sparsify_matrix(weights, nonzero_indices):
		X = numpy.zeros(shape=(len(weights),len(nonzero_indices)))
		i = 0
		for row in weights:
			j = 0
			for col in nonzero_indices:
				X[i][j] = row[col]
				j += 1
			i += 1
		return X 


	#magicNumber = unique binary to identify that the file is a binary model file
	#majorVersion = Major version number of model format (e.g. 1)
	#minorVersion =  Minor version number of model format (e.g. 0)
	#labelID = integer identifier for label section start
	#featureTypeID = integer identifier for feature section start
	#featureTypeExact = integer identifier for feature exact
	#featureTypeHashed = integer identifier for feature hashed
	#featureFeaturesID = integer identifier for feature feature section
	#featureMaxFeatsID = integer identifier for Max Feats section
	#weightsTypeID = integer identifier for weights section start
	#weightsTypeDense = integer identifier for weight type section
	#weightsTypeSparse = integer identifier for weight type section
	#weightsValuesID = integer identifier for weight matrix start

	magicNumber = 0x6A48B9DD
	labelID = 100
	featureTypeID = 110
	featureTypeExact = 1
	featureTypeHashed = 2
	featureFeaturesID = 111
	featureMaxFeatsID = 112
	weightsTypeID = 120
	weightsTypeDense = 1
	weightsTypeSparse = 2
	weightsValuesID = 121

	with io.open(filename, 'wb') as w:
		write_int(w, magicNumber)
		write_short(w, majorVersion)
		write_short(w, minorVersion)
		write_short(w, labelID)
		write_table(w, label_index)
		write_short(w, featureTypeID)
		if hash_trick == True:
			write_short(w, featureTypeHashed)
			write_short(w, featureMaxFeatsID)
			write_int(w, hashmod)
		else:
			write_short(w, featureTypeExact)
			write_short(w, featureFeaturesID)
			write_list(w, feature_index)
		write_short(w, weightsTypeID)
		if sparse == "False":
			write_short(w, weightsTypeDense)
			write_short(w, weightsValuesID)
			write_int(w, len(weights))
			for row in weights:
				write_int(w, len(row))
				for val in row:
					write_double(w, val)
		elif sparse == "True":
			write_short(w, weightsTypeSparse)
			write_short(w, weightsValuesID)
			write_int(w, len(weights))
			nonzero_indices = get_nonzero_indices(weights)
			write_int(w, len(nonzero_indices))
			for i in nonzero_indices:
				write_int(w, i)
			sparse_weights = sparsify_matrix(weights, nonzero_indices)
			for row in sparse_weights:
				for val in row:
					write_double(w, val)

	#print "Done writing to ", filename

def writeJsonModel(model_file, coefs, label_index, feature_index, hash_trick, hashmod):

	def write_feature_info_section(w, feature_index, hash_trick, hashmod):
		parent_dict, write_dict = {}, {}
		if hash_trick == True:
			write_dict["type"] = "hashed"
			write_dict["maxfeats"] = hashmod
		else:
			write_dict["type"] = "exact"
			write_dict["features"] = feature_index

		return write_dict

	#Currently only writes out to dense matrix format for JSON
	def write_weights_section(w, coefs):
		parent_dict, write_dict = {}, {}
		write_dict["type"] = "dense"
		weights = coefs
		write_dict["values"] = weights
		return write_dict

	with open(model_file, 'w') as w:
		feature_section = write_feature_info_section(w, feature_index, hash_trick, hashmod)
		weights_section = write_weights_section(w, coefs)
		parent_dict = {}
		parent_dict["labels"] = label_index
		parent_dict["features"] = feature_section
		parent_dict["weights"] = weights_section
		w.write(json.dumps(parent_dict, indent=4, sort_keys=True))

def readJsonModel(filename):
	with open(model, 'r') as r:
		model_json = json.load(r)
        if model_json['features']['type'] == 'exact':
			weight_matrix = model_json['weights']['values']
        	label_index = model_json['labels']
        	feature_index = model_json['features']['features']
        	hashmod = None
        elif model_json['features']['type'] == 'hashed':
			weight_matrix = model_json['weights']['values']
        	label_index = model_json['labels']        	
        	hashmod = model_json['features']['maxfeats']
        	feature_index = None
    return weight_matrix, label_index, feature_index, hashmod

def readBinaryModel(filename, model_majorVersion, model_minorVersion):
	
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
        if ((majorVersion > model_majorVersion) or (majorVersion == model_majorVersion and minorVersion > model_minorVersion)):
        	print>>sys.stderr, "ERROR"
        	print>>sys.stderr, "Version number greater than anticipated"
        	print>>sys.stderr, "Reading version", str(majorVersion)+"."+str(minorVersion)
        	print>>sys.stderr, "Anticipating version", str(model_majorVersion)+"."+str(model_minorVersion)
        	sys.exit()
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

