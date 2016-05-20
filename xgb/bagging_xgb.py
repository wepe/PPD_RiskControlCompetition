#coding=utf-8

import pandas as pd
import xgboost as xgb
import os,random,cPickle



train = pd.read_csv('../data/8file_train_sample.csv')
train_y = train.target
train_x = train.drop('target',axis=1)

test = pd.read_csv('../data/8file_test.csv')
test_Idx = test.Idx
test_x = test.drop('Idx',axis=1)

all_features =  list(train_x.columns)#1439
features_1 = list(pd.read_csv('../feature_select/feature_score.csv')['feature'])#1157
features_2 = [feature for feature in all_features if feature not in features_1]#282


def pipeline(iteration,random_seed,max_depth,lambd,subsample,colsample_bytree,min_child_weight,n_feature):
    fs = features_1 + features_2[0:n_feature]
    train_selected = train_x[fs]
    test_selected = test_x[fs]

    print train_selected.shape,test_selected.shape

    dtrain = xgb.DMatrix(train_selected,label=train_y)
    dtest = xgb.DMatrix(test_selected)
    
    del train_selected,test_selected

    params={
    	'booster':'gbtree',
    	'objective': 'binary:logistic',
        'eval_metric': 'auc',
    	'max_depth':max_depth,
    	'lambda':lambd,
        'subsample':subsample,
        'colsample_bytree':colsample_bytree,
        'min_child_weight':min_child_weight, 
        'eta': 0.025,
    	'seed':random_seed,
    	'nthread':8
        }
    
    """
    #通过cv找最佳的nround
    cv_log = xgb.cv(params,dtrain,num_boost_round=5000,nfold=8,metrics='auc',early_stopping_rounds=50,show_progress=3,seed=random_seed)
    bst_auc= cv_log['test-auc-mean'].max()
    cv_log['nb'] = cv_log.index
    cv_log.index = cv_log['test-auc-mean']
    nround = cv_log.nb.to_dict()[bst_auc]
    """

    if max_depth==4:
        nround = 1500
    elif max_depth==5:
        nround = 1000

    watchlist  = [(dtrain,'train')]
    model = xgb.train(params,dtrain,num_boost_round=nround,evals=watchlist)

    #predict test set
    test_y = model.predict(dtest)
    test_result = pd.DataFrame(test_Idx,columns=["Idx"])
    test_result["score"] = test_y
    test_result.to_csv("./test/xgb{0}.csv".format(iteration),index=None,encoding='utf-8')



if __name__ == "__main__":
    os.mkdir('test')
    random_seed = range(2016)
    max_depth = [4,5]
    lambd = range(5,41)
    subsample = [i/1000.0 for i in range(700,800)]
    colsample_bytree = [i/1000.0 for i in range(700,800)]
    min_child_weight = [i/100.0 for i in range(150,250)]
    n_feature = range(150,282,2)

    random.shuffle(random_seed)
    random.shuffle(max_depth)
    random.shuffle(lambd)
    random.shuffle(subsample)
    random.shuffle(colsample_bytree)
    random.shuffle(min_child_weight)
    random.shuffle(n_feature)
    
    with open('params.pkl','w') as f:
        cPickle.dump((random_seed,max_depth,lambd,subsample,colsample_bytree,min_child_weight,n_feature),f)
    
    for i in range(36):
        print "iter:",i
        pipeline(i,random_seed[i],max_depth[i%2],lambd[i],subsample[i],colsample_bytree[i],min_child_weight[i],n_feature[i])
