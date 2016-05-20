# coding=UTF-8

import graphlab as gl
import numpy as np
import os


from hyperopt import fmin, hp, tpe
import hyperopt

from sklearn.base import BaseEstimator
from sklearn import preprocessing

MODEL_NAME = 'graphlab_xgboost'

class XGBoost(BaseEstimator):
    def __init__(self, max_iterations=50, max_depth=9, min_child_weight=4.0, row_subsample=.75,
                 min_loss_reduction=1., column_subsample=.8, step_size=.3, verbose=False):
        self.n_classes_ = 2
        self.max_iterations = max_iterations
        self.max_depth = max_depth
        self.min_child_weight = min_child_weight
        self.row_subsample = row_subsample
        self.min_loss_reduction = min_loss_reduction
        self.column_subsample = column_subsample
        self.step_size = step_size
        self.verbose = verbose
        self.model = None

    def fit(self, X, y, sample_weight=None):
        sf = self._array_to_sframe(X, y)
        self.model = gl.boosted_trees_classifier.create(sf, target='target',
                                                        max_iterations=self.max_iterations,
                                                        max_depth=self.max_depth,
                                                        min_child_weight=self.min_child_weight,
                                                        row_subsample=self.row_subsample,
                                                        min_loss_reduction=self.min_loss_reduction,
                                                        column_subsample=self.column_subsample,
                                                        step_size=self.step_size,
                                                        # metric='auc',
                                                        verbose=self.verbose)
        return self

    def predict(self, X):
        preds = self.predict_proba(X)
        return np.argmax(preds, axis=1)

    def predict_proba(self, X):
        sf = self._array_to_sframe(X)
        preds = self.model.predict_topk(sf, output_type='probability', k=self.n_classes_)
        return self._preds_to_array(preds)

    # Private methods
    def _array_to_sframe(self, data, targets=None):
        d = dict()
        for i in xrange(data.shape[1]):
            d['feat_%d' % (i + 1)] = gl.SArray(data[:, i])
        if targets is not None:
            d['target'] = gl.SArray(targets)
        return gl.SFrame(d)

    def _preds_to_array(self, preds):
        p = preds.unstack(['class', 'probability'], 'probs').unpack('probs', '')
        p['id'] = p['id'].astype(int) + 1
        p = p.sort('id')
        del p['id']
        preds_array = np.array(p.to_dataframe(), dtype=float)
        return preds_array

from utils import *
import consts
def test(mode='cv', n_estimators=7000):
    start = clock()

    # X, y, test_x, test_uid = load_data_1()

    X, y, test_x, test_uid = load_data_9()
    # X, y, test_x, test_uid = load_data_8()

    print 'neg:{0},pos:{1}'.format(len(y[y == 0]), len(y[y == 1]))

    # 200 AUC: 0.733611020275  400: AUC: 0.737316873774
    # 300 AUC: 0.7408661707
    # clf = XGBoost(max_iterations=n_estimators, max_depth=8, min_child_weight=4.6333144,
    #               row_subsample=.747, min_loss_reduction=2.8913,
    #               column_subsample=.78, step_size=0.027492)

    # new tune best {'step_size': 0.05285233054338817, 'max_depth': 4.0, 'min_loss_reduction': 2.0167997425431423, 'min_child_weight': 6.207103293214025} 0.759449745856
    # clf = XGBoost(max_iterations=n_estimators, max_depth=4, min_child_weight=6.2071,
    #               row_subsample=.747, min_loss_reduction=2.016799,
    #               column_subsample=.78, step_size=0.052852)

    # {'step_size': 0.05356068930789645, 'max_depth': 4.0, 'min_loss_reduction': 0.9538130074966977, 'min_child_weight': 2.6573367386833606} 0.7597406
    # clf = XGBoost(max_iterations=300, max_depth=4, min_child_weight=2.65733673,
    #               row_subsample=.747, min_loss_reduction=0.9538130,
    #               column_subsample=.78, step_size=0.05356068)
    # {'n_estimators': 500.0, 'step_size': 0.024446901209730865, 'max_depth': 6.0, 'min_loss_reduction': 3.488042867341746, 'min_child_weight': 5.678190184460328} 0.762073498809  (内部有随机种子，可能效果不一定准，自己再测的话)
    # 500 6 778119
    # max_iterations=800, max_depth=7

    clf = XGBoost(max_iterations=800, max_depth=7, min_child_weight=5.67819018446,
                  row_subsample=.747, min_loss_reduction=3.48804,
                  column_subsample=.78, step_size=0.0244469)

    if mode == 'cv':
        scores, predictions = make_blender_cv(clf, X, y)
        print 'CV:', scores
        print 'Mean AUC:', np.mean(scores)
        write_blender_data(consts.BLEND_PATH, MODEL_NAME + '.csv', predictions)
    elif mode == 'submission':
        clf.fit(X, y)
        prediction = clf.predict_proba(test_x)[:, 1]
        save_submission(os.path.join(consts.SUBMISSION_PATH,
                                     MODEL_NAME + '_' + strftime("%m_%d_%H_%M_%S", localtime()) + '.csv'),
                        test_uid, prediction)
    elif mode == 'holdout':
        score = hold_out_evaluation(clf, X, y, calibrate=False, verbose=False)
        print 'AUC:', score
    elif mode == 'tune':
        # Objective function
        def objective(args):
            clf = XGBoost(max_iterations=args['n_estimators'], max_depth=args['max_depth'],
                          min_child_weight=args['min_child_weight'],
                          row_subsample=.747, min_loss_reduction=args['min_loss_reduction'],
                          column_subsample=.78, step_size=args['step_size'])
            scores, predictions = make_blender_cv(clf, X, y)
            score = np.mean(scores)
            # score = hold_out_evaluation(clf, X, y, calibrate=False)
            print args, score
            return 1 - score
        # Searching space
        space = {
            'n_estimators': hp.quniform('n_estimators', 200, 600, 50),
            'min_loss_reduction': hp.uniform('min_loss_reduction', 0.5, 4.0),
            'max_depth': hp.quniform("max_depth", 3, 11, 2),
            'min_child_weight': hp.uniform('min_child_weight', 2, 6),
            'step_size': hp.uniform('step_size', 0.02, 0.1)}
        # best_sln = fmin(objective, space, algo=tpe.suggest, max_evals=400)
        best_sln = fmin(objective, space, algo=hyperopt.anneal.suggest, max_evals=200)
        print 'Best solution:', best_sln
    else:
        print 'Unknown mode'
    finish = clock()
    print('运行时间为：%s 秒' % str(finish - start))

if __name__ == '__main__':
    n_estimators = 500
    start = clock()
    mode = 'cv'  # cv, submission, holdout, tune
    test(mode, n_estimators)

    # mode = 'tune'  # cv, submission, holdout, tune
    # test(mode, n_estimators)

    # mode = 'holdout'  # cv, submission, holdout, tune
    # test(mode, n_estimators)

    mode = 'submission'  # cv, submission, holdout, tune
    test(mode, n_estimators)

    finish = clock()
    print('==============运行时间为：%s 秒==============' % str(finish - start))



