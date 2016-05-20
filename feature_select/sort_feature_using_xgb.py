#coding=utf-8

import pandas as pd
import xgboost as xgb
import os,random,cPickle


os.mkdir('featurescore')


#类别不平衡  2198个正样本（违约），27802个负样本

train = pd.read_csv('../../data/train/train_x_rank.csv')
train_target = pd.read_csv('../../data/train/train_master.csv',encoding='gb18030')[['Idx','target']]
train = pd.merge(train,train_target,on='Idx')
train_y = train.target
train_x = train.drop(['Idx','target'],axis=1)
dtrain = xgb.DMatrix(train_x, label=train_y)

test = pd.read_csv('../../data/test/test_x_rank.csv')
test_Idx = test.Idx
test = test.drop('Idx',axis=1)
dtest = xgb.DMatrix(test)


train_test = pd.concat([train,test])
train_test.to_csv('rank_feature.csv',index=None)
print train_test.shape

"""
params={
    	'booster':'gbtree',
    	'objective': 'rank:pairwise',
    	'scale_pos_weight': float(len(train_y)-sum(train_y))/float(sum(train_y)),
        'eval_metric': 'auc',
    	'gamma':0.1,
    	'max_depth':6,
    	'lambda':500,
        'subsample':0.6,
        'colsample_bytree':0.3,
        'min_child_weight':0.2, 
        'eta': 0.04,
    	'seed':1024,
    	'nthread':8
        }
xgb.cv(params,dtrain,num_boost_round=1100,nfold=10,metrics='auc',show_progress=3,seed=1024)#733

"""

def pipeline(iteration,random_seed,gamma,max_depth,lambd,subsample,colsample_bytree,min_child_weight):
    params={
            'booster':'gbtree',
	    'objective': 'rank:pairwise',
	    'scale_pos_weight': float(len(train_y)-sum(train_y))/float(sum(train_y)),
	    'eval_metric': 'auc',
	    'gamma':gamma,
	    'max_depth':max_depth,
	    'lambda':lambd,
	    'subsample':subsample,
	    'colsample_bytree':colsample_bytree,
	    'min_child_weight':min_child_weight, 
	    'eta': 0.2,
	    'seed':random_seed,
	    'nthread':8
	 }

    watchlist  = [(dtrain,'train')]
    model = xgb.train(params,dtrain,num_boost_round=700,evals=watchlist)
    #model.save_model('./model/xgb{0}.model'.format(iteration))
    #predict test set
    #test_y = model.predict(dtest)
    #test_result = pd.DataFrame(test_Idx,columns=["Idx"])
    #test_result["score"] = test_y
    #test_result.to_csv("./preds/xgb{0}.csv".format(iteration),index=None,encoding='utf-8')
    
    #save feature score
    feature_score = model.get_fscore()
    feature_score = sorted(feature_score.items(), key=lambda x:x[1],reverse=True)
    fs = []
    for (key,value) in feature_score:
        fs.append("{0},{1}\n".format(key,value))
    
    with open('./featurescore/feature_score_{0}.csv'.format(iteration),'w') as f:
        f.writelines("feature,score\n")
        f.writelines(fs)


if __name__ == "__main__":
    random_seed = range(10000,20000,100)
    gamma = [i/1000.0 for i in range(0,300,3)]
    max_depth = [5,6,7]
    lambd = range(400,600,2)
    subsample = [i/1000.0 for i in range(500,700,2)]
    colsample_bytree = [i/1000.0 for i in range(550,750,4)]
    min_child_weight = [i/1000.0 for i in range(250,550,3)]
    
    random.shuffle(random_seed)
    random.shuffle(gamma)
    random.shuffle(max_depth)
    random.shuffle(lambd)
    random.shuffle(subsample)
    random.shuffle(colsample_bytree)
    random.shuffle(min_child_weight)
    
    with open('params.pkl','w') as f:
        cPickle.dump((random_seed,gamma,max_depth,lambd,subsample,colsample_bytree,min_child_weight),f)

    for i in range(36):
        pipeline(i,random_seed[i],gamma[i],max_depth[i%3],lambd[i],subsample[i],colsample_bytree[i],min_child_weight[i])
