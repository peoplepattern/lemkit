
class LinearModel:

    majorVersion = 1
    minorVersion = 1

    hash_trick = False
    hashmod = None

    train_file, model_file = "", ""
    model_type, model_method = "", ""

    label_index = None
    feature_index = None
    weight_matrix = []
    # nonzero_index only used with sparse binary models
    nonzero_index = None

    def __init__(self, label_index, feature_index, weight_matrix, nonzero_index=None, hash_trick=False, hashmod=None):
        self.label_index = label_index
        self.feature_index = feature_index
        self.weight_matrix = weight_matrix

        self.hash_trick = hash_trick
        self.hashmod = hashmod
        self.nonzero_index = nonzero_index

    def predict(self, predict_file, outfile=None):

        from lemkit.predict import *

        if self.feature_index is not None:
            if self.nonzero_index is not None:
                predictions = exact_predict_sparse(self.weight_matrix,
                                                   self.label_index,
                                                   self.feature_index,
                                                   predict_file,
                                                   self.nonzero_indices)
            else:
                predictions = exact_predict(self.weight_matrix,
                                            self.label_index,
                                            self.feature_index,
                                            predict_file)
        elif self.hashmod is not None:
            if self.nonzero_indices is not None:
                predictions = hashed_predict_sparse(self.weight_matrix,
                                                    self.label_index,
                                                    self.hashmod,
                                                    predict_file,
                                                    self.nonzero_indices)
            else:
                predictions = hashed_predict(self.weight_matrix,
                                             self.label_index,
                                             self.hashmod,
                                             predict_file)
        else:
            print "ERROR"
            print "Feature Index and hashmod not set", self.feature_index, self.hashmod

        if outfile is not None:
            with io.open(outfile, 'w', encoding='utf-8') as w:
                for p in predictions:
                    w.write(u' '.join(p[:-1]) + '\n')
            print "Writing predictions complete check them @ ", outfile

        return predictions

    def writeBinary(self, model_file, sparse="False"):
        from lemkit import model_tools
        model_tools.writeBinaryModel(model_file, self.weight_matrix,
                                     self.feature_index, self.label_index,
                                     self.hash_trick, self.hashmod,
                                     self.majorVersion, self.minorVersion,
                                     sparse)

    # Currently only writes dense matrix format
    def writeJson(self, model_file):
        from lemkit import model_tools
        model_tools.writeJsonModel(model_file, self.weight_matrix,
                                   self.label_index, self.feature_index,
                                   self.hash_trick, self.hashmod)
