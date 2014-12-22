#The purpose of the script is to encourage model serialization and
#interoperability between models trained in Vowpal_Wabbit and Scikit-Learn
#And to encourage portability between these models for Python, Scala, Go

#This script takes in a file of features, it then:
#(1) Hashes the features using murmurhash 3 implementation
#(2) trains using a SciKit linear model
#(3) saves the model into a portable json forat

import io, sys

import numpy, scipy

from sklearn import linear_model
from operator import itemgetter
import lemkit_train

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
		lemkit_train.json_save(model_file, coefs, label_index, feature_index,
			  hash_trick, hashmod)
	if save_mode == "binary":
		lemkit_train.writeBinaryModel(model_file, coefs, feature_index, label_index,
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
