import json

def writeBinaryModel(filename, weights, feature_index, label_index,
					 hash_trick, hashmod, sparse="False"):
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
		for col in weights.transpose():
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
	majorVersion = 1
	minorVersion = 0
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

def json_save(model_file, coefs, label_index, feature_index, hash_trick, hashmod):

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
		write_dict["values"] = weights.tolist()
		return write_dict

	with open(model_file, 'w') as w:
		feature_section = write_feature_info_section(w, feature_index, hash_trick, hashmod)
		weights_section = write_weights_section(w, coefs)
		parent_dict = {}
		parent_dict["labels"] = label_index
		parent_dict["features"] = feature_section
		parent_dict["weights"] = weights_section
		w.write(json.dumps(parent_dict, indent=4, sort_keys=True))
