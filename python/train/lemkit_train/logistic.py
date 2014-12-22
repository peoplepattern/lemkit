#The purpose of the script is to encourage model serialization and
#interoperability between models trained in Vowpal_Wabbit and Scikit-Learn
#And to encourage portability between these models for Python, Scala, Go

#This script takes in a file of features, it then:
#(1) Hashes the features using murmurhash 3 implementation
#(2) trains using a SciKit linear model
#(3) saves the model into a portable json forat

import io, sys

import numpy, scipy

import json
from sklearn import linear_model
from operator import itemgetter

try:
	import mmh3 as mh
except:
	print>>sys.stderr, "mmh3 not installed, switching to slow Python implementation"
	import murmurh3 as mh

#trainfile format:
#
#label | feature:value feature:value feature:value
#

#trainfile = file path with format specified above
#regularizaiton = L1 or L2
#model_file = file name where trained model will be written to
#save_mode = "json" or "binary"
#hash_trick = True or False. If true it will use murmurhash3 and the hashing trick
#	to map strings to features. If false it will create a sequential mapping of features
#	from 1 to N where N = number of features
#hashmod = hashing trick constrains number of unique features to a given integer
#	defaults to 1,000,000
#output_hash = if set to True it will output a schema file that describes
#	mapping of integers to native feature strings
#output_hash_file = if output_hash is set to true the feature mapping will be
#	written to this file
def train(trainfile, model_file, save_mode="json", hash_trick=False,  regularization="L1",
				   hashmod=1000000, output_hash=False, sparse_output="False", output_hash_file="tmp_hash.schema.txt"):

	Y, X, label_index, feature_index = extract_vectors(trainfile, hash_trick, hashmod,
													   output_hash, output_hash_file)


	#fit_intercept = False means scitkit does not automatically add constant feature
	# we do this manually so we set to False
	model = linear_model.LogisticRegression(penalty=regularization.lower(), fit_intercept=False)

	model.fit(X, Y)

	coefs = model.coef_

	if save_mode == "json":
		json_save(model_file, coefs, label_index, feature_index,
			  hash_trick, hashmod)
	if save_mode == "binary":
		WriteBinaryModel(model_file, coefs, feature_index, label_index,
						 hash_trick, hashmod, sparse=sparse_output)

	print "Training Complete Check model file @ ", model_file

#Vectorize input data into relevant scipy matrices
def extract_vectors(trainfile, hash_trick, hashmod, output_hash, output_hash_file):
	X = []
	Y = []

	feature_index, label_index = {}, {}
	i, j, y = 0, 0, 0
	row_indexes, col_indexes, values, labels = [], [], [], []

	feature_index[u"".encode('utf-8')] = 0

	i += 1

	with io.open(trainfile, 'r', encoding='utf-8') as tf:
		for line in tf:
			if len(line) > 0:
				label = line.strip().split(u'|')[0].strip()
				feats = line.strip().split(u'|')[1].strip()
				row_indexes.append(len(values))
				#Add the intercept constant
				col_indexes.append(0)
				values.append(1.0)
				j += 1
				if label not in label_index:
					label_index[label] = y
					y += 1
				labels.append(label_index[label])
				for f in feats.split(u' '):
					fv = f.split(u':')
					if fv[0] not in feature_index:
						if hash_trick == True:
							feature_index[fv[0]] = mh.hash(fv[0].encode('utf-8')) % hashmod
						else:
							feature_index[fv[0]] = i
							i += 1
					col_indexes.append(feature_index[fv[0]])
					values.append(fv[1])
		row_indexes.append(row_indexes[-1]+1)
	if hash_trick == True:
		i = hashmod

	#If output_hash is true then write hashing schema to tab delimited tmp file
	if output_hash == True:
		with io.open(output_hash_file, 'w', encoding='utf-8') as w:
			for feat_val in feature_index:
				w.write(feat_val + '\t' + unicode(feature_index[feat_val]) + '\r\n')

	X = scipy.sparse.csr_matrix((scipy.array(values, dtype=scipy.float64), scipy.array(col_indexes),
	 scipy.array(row_indexes)), shape=(j, i), dtype=scipy.float64)
	Y = scipy.array(labels)

	return Y, X, label_index, feature_index

def move_features_right(feature_index):
	new_feature_index = {}
	for f in feature_index:
		new_feature_index[f] = feature_index[f]+1
	return new_feature_index

def WriteBinaryModel(filename, weights, feature_index, label_index,
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
