#coding=utf-8

"""
Author: wepon (http://2hwp.com/)

"""

import pandas as pd



"""
#只统计数值特征的缺失值个数，后面SVM会用到

feature_type = pd.read_excel('../data/feature_type.xlsx')
feature_type.columns = ['feature','type']
numerical_feature = list(feature_type[feature_type.type=='Numerical']['feature'])
numerical_feature.remove('target')
numerical_feature.remove('ListingInfo')


train_x = pd.read_csv('../data/train/PPD_Training_Master_GBK_3_1_Training_Set.csv',encoding='gbk')[['Idx','target']+numerical_feature]
train_x.fillna(-1,inplace=True)

train_x['n_null'] = (train_x<0).sum(axis=1)

train_missing_gt100= train_x[train_x.n_null>100]
train_missing_gt100[['Idx','n_null','target']].to_csv('../data/train_missing_gt100.csv',index=None)


"""



train_x = pd.read_csv('../data/train/train_master.csv',encoding='gb18030')
train_x.fillna(-1,inplace=True)
test_x = pd.read_csv('../data/test/Kesci_Master_9w_gbk_1_test_set.csv',encoding='gb18030')
test_x.fillna(-1,inplace=True)

train_x['n_null'] = (train_x<0).sum(axis=1)
test_x['n_null'] = (test_x<0).sum(axis=1)


train_x['discret_null'] = train_x.n_null
train_x.discret_null[train_x.discret_null<=24] = 1
train_x.discret_null[(train_x.discret_null>24)&(train_x.discret_null<=34)] = 2
train_x.discret_null[(train_x.discret_null>34)&(train_x.discret_null<=46)] = 3
train_x.discret_null[(train_x.discret_null>46)&(train_x.discret_null<=51)] = 4
train_x.discret_null[(train_x.discret_null>51)] = 5
train_x[['Idx','n_null','discret_null']].to_csv('../data/train/train_x_null.csv',index=None)

test_x['discret_null'] = test_x.n_null
test_x.discret_null[test_x.discret_null<=24] = 1
test_x.discret_null[(test_x.discret_null>24)&(test_x.discret_null<=34)] = 2
test_x.discret_null[(test_x.discret_null>34)&(test_x.discret_null<=46)] = 3
test_x.discret_null[(test_x.discret_null>46)&(test_x.discret_null<=51)] = 4
test_x.discret_null[(test_x.discret_null>51)] = 5
test_x[['Idx','n_null','discret_null']].to_csv('../data/test/test_x_null.csv',index=None)




