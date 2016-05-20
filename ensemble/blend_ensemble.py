# coding=UTF-8


from __future__ import division
import numpy as np
from sklearn.cross_validation import StratifiedKFold
from sklearn.ensemble import RandomForestClassifier, ExtraTreesClassifier, GradientBoostingClassifier
from sklearn.linear_model import LogisticRegression
from utils import *
from sklearn import cross_validation, linear_model

# MAX_ITERS = 1
# N_TREES = 5
# XGBC_TREES = 5
# LM_CV_NUM = 5

N_TREES = 500
XGBC_TREES = 1500
LM_CV_NUM = 100
MAX_ITERS = 400

INITIAL_PARAMS = {
    'LR:one': {'C': 2, 'penalty': 'l2', 'class_weight': 'balanced', 'n_jobs': -1,
               'max_iter': MAX_ITERS,
               },
    'XGBC:one': {
        'n_estimators': XGBC_TREES,
        'scale_pos_weight': 6,
        'objective': 'binary:logistic',
        'learning_rate': 0.02,
        'gamma': 0.7,
        'reg_lambda': 800,
        'colsample_bytree': 0.75,
        'max_depth': 5,
        'min_child_weight': 4,
        'subsample': 0.75,
        },
    'XGBC:two': {
        'n_estimators': 400,
        'scale_pos_weight': 1,
        'objective': 'binary:logistic',
        'learning_rate': 0.04,
        'colsample_bytree': 0.75,
        'max_depth': 5,
        'subsample': 0.75,
        },
    'RFC:one': {
        # 'n_estimators': N_TREES, 'n_jobs': -1, 'criterion': 'entropy',
        # 'min_samples_leaf': 2, 'bootstrap': False,
        # 'max_depth': 20, 'min_samples_split': 7, 'max_features': 0.5
        # new tune
         'n_estimators': 400, 'n_jobs': -1, 'criterion': 'entropy',
        'min_samples_leaf': 3.0, 'bootstrap': False,
        'max_depth': 12, 'min_samples_split': 6.0, 'max_features': 0.14357
    },
    'ETC:one': {
        'n_estimators': N_TREES, 'n_jobs': -1, 'min_samples_leaf': 2,  'criterion': 'entropy',
        'max_depth': 20, 'min_samples_split': 5, 'max_features': 0.4,
        'bootstrap': False,
        },
    'GBC:one': {
        'n_estimators': int(N_TREES / 2), 'learning_rate': .08, 'max_features': 0.5,
        'min_samples_leaf': 1, 'min_samples_split': 3, 'max_depth': 5,
        },
    }

import xgboost as xgb

MODEL_NAME = 'blend_ensemble'

def offline(X_org, y_org):
    n_folds = 5
    verbose = True
    shuffle = False

    x_train, y_train, x_valid, y_valid = stratified_split(X_org, y_org, 0.2)

    X = x_train
    y = y_train
    X_submission = x_valid
    X_submission_y = y_valid

    if shuffle:
        idx = np.random.permutation(y.size)
        X = X[idx]
        y = y[idx]

    skf = list(StratifiedKFold(y, n_folds))

    clfs = [
        RandomForestClassifier().set_params(**INITIAL_PARAMS.get("RFC:one", {})),
        ExtraTreesClassifier().set_params(**INITIAL_PARAMS.get("ETC:one", {})),
        GradientBoostingClassifier().set_params(**INITIAL_PARAMS.get("GBC:one", {})),
        LogisticRegression().set_params(**INITIAL_PARAMS.get("LR:one", {})),
        xgb.XGBClassifier().set_params(**INITIAL_PARAMS.get("XGBC:two", {})),
        xgb.XGBClassifier().set_params(**INITIAL_PARAMS.get("XGBC:one", {})),
        ]

    print "Creating train and test sets for blending."

    dataset_blend_train = np.zeros((X.shape[0], len(clfs)))
    dataset_blend_test = np.zeros((X_submission.shape[0], len(clfs)))

    for j, clf in enumerate(clfs):
        print j, clf
        dataset_blend_test_j = np.zeros((X_submission.shape[0], len(skf)))
        for i, (train, test) in enumerate(skf):
            print "Fold", i
            X_train = X[train]
            y_train = y[train]
            X_test = X[test]
            y_test = y[test]
            clf.fit(X_train, y_train)
            y_submission = clf.predict_proba(X_test)[:,1]
            dataset_blend_train[test, j] = y_submission
            dataset_blend_test_j[:, i] = clf.predict_proba(X_submission)[:,1]
        dataset_blend_test[:,j] = dataset_blend_test_j.mean(1)
        print("val auc Score: %0.5f" % (compute_auc(X_submission_y, dataset_blend_test[:,j])))

    print "Blending."
    # clf = LogisticRegression(C=2, penalty='l2', class_weight='balanced', n_jobs=-1)
    clf = linear_model.RidgeCV(
            alphas=np.linspace(0, 200), cv=LM_CV_NUM)
    # clf = GradientBoostingClassifier(learning_rate=0.02, subsample=0.5, max_depth=6, n_estimators=100)
    clf.fit(dataset_blend_train, y)
    # y_submission = clf.predict_proba(dataset_blend_test)[:,1]
    print clf.coef_, clf.intercept_
    y_submission = clf.predict(dataset_blend_test)  # for RidgeCV

    print "Linear stretch of predictions to [0,1]"
    y_submission = (y_submission - y_submission.min()) / (y_submission.max() - y_submission.min())
    print "blend result"
    print("val auc Score: %0.5f" % (compute_auc(X_submission_y, y_submission)))

import consts

def online(X_org, y_org, test_x, test_uid):
    n_folds = 5
    verbose = True
    shuffle = False

    X = X_org
    y = y_org
    X_submission = test_x

    if shuffle:
        idx = np.random.permutation(y.size)
        X = X[idx]
        y = y[idx]

    skf = list(StratifiedKFold(y, n_folds))

    clfs = [
        RandomForestClassifier().set_params(**INITIAL_PARAMS.get("RFC:one", {})),
        ExtraTreesClassifier().set_params(**INITIAL_PARAMS.get("ETC:one", {})),
        GradientBoostingClassifier().set_params(**INITIAL_PARAMS.get("GBC:one", {})),
        LogisticRegression().set_params(**INITIAL_PARAMS.get("LR:one", {})),
        xgb.XGBClassifier().set_params(**INITIAL_PARAMS.get("XGBC:two", {})),
        xgb.XGBClassifier().set_params(**INITIAL_PARAMS.get("XGBC:one", {})),
        ]

    print "Creating train and test sets for blending."

    dataset_blend_train = np.zeros((X.shape[0], len(clfs)))
    dataset_blend_test = np.zeros((X_submission.shape[0], len(clfs)))

    for j, clf in enumerate(clfs):
        print j, clf
        dataset_blend_test_j = np.zeros((X_submission.shape[0], len(skf)))
        for i, (train, test) in enumerate(skf):
            print "Fold", i
            X_train = X[train]
            y_train = y[train]
            X_test = X[test]
            y_test = y[test]
            clf.fit(X_train, y_train)
            y_submission = clf.predict_proba(X_test)[:,1]
            dataset_blend_train[test, j] = y_submission
            dataset_blend_test_j[:, i] = clf.predict_proba(X_submission)[:,1]
        dataset_blend_test[:,j] = dataset_blend_test_j.mean(1)

    print "Blending."
    # clf = LogisticRegression(C=2, penalty='l2', class_weight='balanced', n_jobs=-1)
    clf = linear_model.RidgeCV(
            alphas=np.linspace(0, 200), cv=LM_CV_NUM)
    # clf = GradientBoostingClassifier(learning_rate=0.02, subsample=0.5, max_depth=6, n_estimators=100)
    clf.fit(dataset_blend_train, y)
    # y_submission = clf.predict_proba(dataset_blend_test)[:,1]
    print clf.coef_, clf.intercept_
    y_submission = clf.predict(dataset_blend_test)  # for RidgeCV

    print "Linear stretch of predictions to [0,1]"
    y_submission = (y_submission - y_submission.min()) / (y_submission.max() - y_submission.min())
    print "blend result"
    save_submission(os.path.join(consts.SUBMISSION_PATH,
                                     MODEL_NAME + '_' + strftime("%m_%d_%H_%M_%S", localtime()) + '.csv'),
                        test_uid, y_submission)

def online2(X_org, y_org, test_x, test_uid):
    n_folds = 5
    verbose = True
    shuffle = False

    X = X_org
    y = y_org
    X_submission = test_x

    if shuffle:
        idx = np.random.permutation(y.size)
        X = X[idx]
        y = y[idx]

    skf = list(StratifiedKFold(y, n_folds))

    clfs = [
        RandomForestClassifier().set_params(**INITIAL_PARAMS.get("RFC:one", {})),
        ExtraTreesClassifier().set_params(**INITIAL_PARAMS.get("ETC:one", {})),
        GradientBoostingClassifier().set_params(**INITIAL_PARAMS.get("GBC:one", {})),
        LogisticRegression().set_params(**INITIAL_PARAMS.get("LR:one", {})),
        # xgb.XGBClassifier().set_params(**INITIAL_PARAMS.get("XGBC:two", {})),
        # xgb.XGBClassifier().set_params(**INITIAL_PARAMS.get("XGBC:one", {})),
        ]

    print "Creating train and test sets for blending."

    dataset_blend_train = np.zeros((X.shape[0], len(clfs)))
    dataset_blend_test = np.zeros((X_submission.shape[0], len(clfs)))

    for j, clf in enumerate(clfs):
        print j, clf
        dataset_blend_test_j = np.zeros((X_submission.shape[0], len(skf)))
        for i, (train, test) in enumerate(skf):
            print "Fold", i
            X_train = X[train]
            y_train = y[train]
            clf.fit(X_train, y_train)
            dataset_blend_test_j[:, i] = clf.predict_proba(X_submission)[:, 1]
        dataset_blend_test[:,j] = dataset_blend_test_j.mean(1)
        save_submission(os.path.join(consts.SUBMISSION_PATH,
                                         clf.__class__.__name__ + '_' + strftime("%m_%d_%H_%M_%S", localtime()) + '.csv'),
                            test_uid, dataset_blend_test[:, j])


if __name__ == '__main__':

    np.random.seed(0)  # seed to shuffle the train set

    X_org, y_org, test_x, test_uid = load_data_1()

    offline(X_org, y_org)
    online(X_org, y_org, test_x, test_uid)
    # online2(X_org, y_org, test_x, test_uid)

