#coding=utf-8

"""
Author: wepon (http://2hwp.com/)

"""

import pandas as pd

feature_type = pd.read_excel('../data/feature_type.xlsx')
feature_type.columns = ['feature','type']
numerical_feature = list(feature_type[feature_type.type=='Numerical']['feature'])
numerical_feature.remove('target')
numerical_feature.remove('ListingInfo')


train = pd.read_csv('../data/train/train_master.csv',encoding='gb18030')[['Idx','target']+numerical_feature]
train[train==-1] = None
test = pd.read_csv('../data/test/Kesci_Master_9w_gbk_1_test_set.csv',encoding='gb18030')[['Idx']+numerical_feature]
test['target'] = [-99999 for i in range(len(test))]
test[test==-1] = None
train_test = pd.concat([train,test])
train_test[train_test.target==1].fillna(train_test[train_test.target==1].median(),inplace=True)
train_test[train_test.target==0].fillna(train_test[train_test.target==0].median(),inplace=True)
train_test.fillna(train_test.median(),inplace=True)

#数值特征
train = train_test[train_test.target!=-99999]
train.drop('target',axis=1,inplace=True)
train.to_csv('../data/train/train_x_numeric.csv',index=None)

test = train_test[train_test.target==-99999]
test.drop('target',axis=1,inplace=True)
test.to_csv('../data/test/test_x_numeric.csv',index=None)

#rank特征
train_test_rank = train_test[['Idx','target']]
for feature in numerical_feature:
    train_test_rank['r'+feature] = train_test[feature].rank(method='max')/float(len(train_test))

train_rank = train_test_rank[train_test_rank.target!=-99999]
train_rank.drop('target',axis=1,inplace=True)
train_rank.to_csv('../data/train/train_x_rank.csv',index=None)

test_rank = train_test_rank[train_test_rank.target==-99999]
test_rank.drop('target',axis=1,inplace=True)
test_rank.to_csv('../data/test/test_x_rank.csv',index=None)
