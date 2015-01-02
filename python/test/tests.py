from nose.tools import *
import os, inspect, sys

import lemkit

from lemkit.train import logistic

LEMKIT = os.environ['LEMKIT']

datadir = os.path.join(LEMKIT, 'data')

def reframe_preds(preds):
	return [[int(p[0]), unicode(p[1].decode('utf-8')), unicode(p[2].decode('utf-8'))] for p in preds]

def reframe_preds2(preds):
	return [[int(p[0]), unicode(p[1]), unicode(p[2])] for p in preds]

def reframe_preds3(preds):
	return [[int(p[0]), p[1].decode('utf-8'), p[2].decode('utf-8')] for p in preds]

def test_logistic_iris():

	global datadir

	gold_preds = [[1, u"Iris-setosa", u"Iris-setosa"],
	[2, u"Iris-versicolor", u"Iris-versicolor"],
	[3, u"Iris-versicolor", u"Iris-versicolor"],
	[4, u"Iris-setosa", u"Iris-setosa"],
	[5, u"Iris-setosa", u"Iris-setosa"],
	[6, u"Iris-setosa", u"Iris-setosa"],
	[7, u"Iris-virginica", u"Iris-virginica"],
	[8, u"Iris-setosa", u"Iris-setosa"],
	[9, u"Iris-versicolor", u"Iris-versicolor"],
	[10, u"Iris-setosa", u"Iris-setosa"],
	[11, u"Iris-virginica", u"Iris-virginica"],
	[12, u"Iris-versicolor", u"Iris-versicolor"],
	[13, u"Iris-virginica", u"Iris-virginica"],
	[14, u"Iris-virginica", u"Iris-virginica"],
	[15, u"Iris-versicolor", u"Iris-versicolor"],
	[16, u"Iris-versicolor", u"Iris-versicolor"],
	[17, u"Iris-setosa", u"Iris-setosa"],
	[18, u"Iris-setosa", u"Iris-setosa"],
	[19, u"Iris-versicolor", u"Iris-versicolor"],
	[20, u"Iris-setosa", u"Iris-setosa"],
	[21, u"Iris-virginica", u"Iris-virginica"],
	[22, u"Iris-versicolor", u"Iris-versicolor"],
	[23, u"Iris-versicolor", u"Iris-versicolor"],
	[24, u"Iris-virginica", u"Iris-virginica"],
	[25, u"Iris-virginica", u"Iris-virginica"],
	[26, u"Iris-versicolor", u"Iris-virginica"],
	[27, u"Iris-versicolor", u"Iris-virginica"],
	[28, u"Iris-virginica", u"Iris-virginica"],
	[29, u"Iris-setosa", u"Iris-setosa"],
	[30, u"Iris-setosa", u"Iris-setosa"]]

	gold_preds_utf = [[1, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[2, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[3, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[4, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[5, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[6, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[7, u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[8, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[9, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[10, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[11, u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[12, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[13, u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[14, u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[15, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[16, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[17, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[18, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[19, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[20, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[21, u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[22, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[23, u'\xdfen-\u6771\u4eac', u'\xdfen-\u6771\u4eac'],
	[24, u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[25, u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[26, u'\xdfen-\u6771\u4eac', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'], [27, u'\xdfen-\u6771\u4eac', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[28, u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica', u'\u0448\u044f-\u0627\u0644\u0631\u064a\u0627\u0636\u200e-virginica'],
	[29, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa'],
	[30, u'\xdcbe\u0636-setosa', u'\xdcbe\u0636-setosa']]

	gold_preds_utf = [[r[0], r[1], r[2]] for r in gold_preds_utf]

	td = os.path.join(datadir, 'iris.train.txt')
	td_utf = os.path.join(datadir, 'iris.utf.train.txt')
	tst = os.path.join(datadir, 'iris.test.txt')
	tst_utf = os.path.join(datadir, 'iris.utf.test.txt')
	model = logistic.train(td, hash_trick=False, regularization="L1")
	predictions = reframe_preds(model.predict(tst))
	#print "Evaluating regular"
	#print predictions
	assert predictions == gold_preds, '%s != %s' % (predictions, gold_preds)
	model = logistic.train(td_utf, hash_trick=False, regularization="L1")
	predictions = reframe_preds3(model.predict(tst_utf))
	#print "Evaluating utf"
	assert predictions == gold_preds_utf, '%s != %s' % (predictions, gold_preds_utf)





