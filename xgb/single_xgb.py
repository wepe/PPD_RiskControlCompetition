#coding=utf-8

"""
import pandas as pd
import xgboost as xgb
from sklearn.cross_validation import train_test_split

train = pd.read_csv('./data/8file_train_sample.csv')
train_y = train.target
train_x = train.drop('target',axis=1)
dtrain = xgb.DMatrix(train_x, label=train_y)
print train_x

params={
        'booster':'gbtree',
	'objective': 'binary:logistic',
	'eval_metric': 'auc',
	'max_depth':4,
	'lambda':10,
	'subsample':0.75,
	'colsample_bytree':0.75,
	'min_child_weight':2, 
	'eta': 0.025,
	'seed':0,
	'nthread':8,
        'silent':1
      }
watchlist  = [(dtrain,'train')]
#xgb.cv(params,dtrain,num_boost_round=1500,nfold=5,metrics='auc',early_stopping_rounds=50,show_progress=3)
xgb.train(params,dtrain,num_boost_round=1500,evals=watchlist)# train-auc:0.918821
"""



import pandas as pd
import xgboost as xgb
from sklearn.cross_validation import train_test_split

features = list(pd.read_csv('feature_select/feature_score.csv')['feature'])

train = pd.read_csv('./data/8file_train_sample.csv')
y = train.target
x = train.drop('target',axis=1)[features]
train_x,val_x,train_y,val_y = train_test_split(x,y,test_size=0.1,random_state=0)

dtrain = xgb.DMatrix(train_x, label=train_y)
dval = xgb.DMatrix(val_x, label=val_y)
print train_x.shape

params={
        'booster':'gbtree',
	'objective': 'binary:logistic',
	'eval_metric': 'auc',
	'max_depth':4,
	'lambda':10,
	'subsample':0.75,
	'colsample_bytree':0.75,
	'min_child_weight':2, 
	'eta': 0.025,
	'seed':0,
	'nthread':8,
        'silent':1
      }
watchlist  = [(dtrain,'train'),(dval,'val')]
xgb.cv(params,dtrain,num_boost_round=1500,nfold=5,metrics='auc',early_stopping_rounds=50,show_progress=3)#cv接近0.79
