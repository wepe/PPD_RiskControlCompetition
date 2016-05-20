#coding=utf-8

"""
Author: wepon (http://2hwp.com/)

"""

import sys,random,os,cPickle
from multiprocessing import Pool
from sklearn.svm import SVC
from sklearn import metrics,cross_validation
from sklearn.cross_validation import train_test_split
from sklearn.externals import joblib
import pandas as pd



#异常数据
outliers = list(pd.read_csv('../data/outlier.csv')['Idx'])



#这35个是从feature select/city feature的feature score的top里选的
city_selected = ['city_198','city_19','city_3','city_182','city_163','city_6','city_287','city_175','city_279','city_122','city_160','city_205','city_329','city_123','city_13','city_97','city_87','city_147','city_228',  'city_86','city_331','city_103','city_336','city_172','city_120','city_113','city_266','city_191','city_66','city_90','city_276','city_150','city_124','city_341','city_246']


#u19 u20
train_city = pd.read_csv('../data/train/train_city.csv')[['Idx']+city_selected]
test_city = pd.read_csv('../data/test/test_city.csv')[['Idx']+city_selected]


#类别特征
train_test_category = pd.read_csv('../data/tianyin/category_feature_onehotencode.csv')


#经纬度特征
train_test_jw = pd.read_csv('../data/jin_wei.csv')
train_test_jw.fillna(train_test_jw.median(),inplace=True)
train_test_jw['U42_j'] = train_test_jw.U4_j - train_test_jw.U2_j
train_test_jw['U42_w'] = train_test_jw.U4_w - train_test_jw.U2_w

u2j_min = train_test_jw.U2_j.min()
u2j_max = train_test_jw.U2_j.max()
u4j_min = train_test_jw.U4_j.min()
u4j_max = train_test_jw.U4_j.max()
u42j_min = train_test_jw.U42_j.min()
u42j_max = train_test_jw.U42_j.max()
u2w_min = train_test_jw.U2_w.min()
u2w_max = train_test_jw.U2_w.max()
u4w_min = train_test_jw.U4_w.min()
u4w_max = train_test_jw.U4_w.max()
u42w_min = train_test_jw.U42_w.min()
u42w_max = train_test_jw.U42_w.max()

train_test_jw.U2_j = train_test_jw.U2_j.apply(lambda x:float(x-u2j_min)/float(u2j_max-u2j_min))
train_test_jw.U2_w = train_test_jw.U2_w.apply(lambda x:float(x-u2w_min)/float(u2w_max-u2w_min))
train_test_jw.U4_j = train_test_jw.U4_j.apply(lambda x:float(x-u4j_min)/float(u4j_max-u4j_min))
train_test_jw.U4_w = train_test_jw.U4_w.apply(lambda x:float(x-u4w_min)/float(u4w_max-u4w_min))
train_test_jw.U42_j = train_test_jw.U42_j.apply(lambda x:float(x-u42j_min)/float(u42j_max-u42j_min))
train_test_jw.U42_w = train_test_jw.U42_w.apply(lambda x:float(x-u42w_min)/float(u42w_max-u42w_min))

#listinginfo特征
train_listinginfo = pd.read_csv('../data/train/train_listinginfo.csv')[['Idx','ListingInfo']]
test_listinginfo = pd.read_csv('../data/test/test_listinginfo.csv')[['Idx','ListingInfo']]
train_test_listinginfo = pd.concat([train_listinginfo,test_listinginfo])
train_test_listinginfo.ListingInfo = train_test_listinginfo.ListingInfo.apply(lambda x:float(x)/train_test_listinginfo.ListingInfo.max())


#rank 特征
test = pd.read_csv("../data/test/test_x_rank.csv")
test = pd.merge(test,train_test_listinginfo,on='Idx',how='left')
test = pd.merge(test,test_city,on='Idx',how='left')
test = pd.merge(test,train_test_jw,on='Idx',how='left')
test = pd.merge(test,train_test_category,on='Idx',how='left')
test_Idx = test.Idx
test_x = test.drop("Idx",axis=1)
        
train = pd.read_csv('../data/train/train_x_rank.csv')
train_target = pd.read_csv('../data/train/train_master.csv',encoding='gb18030')[['Idx','target']]
train = pd.merge(train,train_target,on='Idx')
train = pd.merge(train,train_test_listinginfo,on='Idx',how='left')
train = pd.merge(train,train_city,on='Idx',how='left')
train = pd.merge(train,train_test_jw,on='Idx',how='left')
train = pd.merge(train,train_test_category,on='Idx',how='left')
#生成validation set，并从trainset里去掉validation set
val = pd.read_csv('../data/validation/validation_set.csv')
val = pd.merge(val,train,on="Idx",how='left')
val.drop('target_x',axis=1,inplace=True)
val.rename(columns={'target_y':'target'},inplace=True)
val_idx = list(val.Idx)
train.index = train.Idx
train.drop(val_idx,axis=0,inplace=True)
val_Idx = val.Idx

for idx in outliers:
    train = train[train.Idx!=idx]

# remove constant columns
remove = [] 
for col in train.columns:
    if train[col].std() == 0:
        remove.append(col)
train.drop(remove, axis=1, inplace=True)
test_x.drop(remove, axis=1, inplace=True)
val.drop(remove, axis=1, inplace=True)



train_y = train.target
train_x = train.drop(['Idx','target'],axis=1)
val_y = val.target
val_x = val.drop(['Idx','target'],axis=1)

print test_x.shape
print train_x.shape
print val_x.shape


del train_city,test_city,train_test_category,train_listinginfo,test_listinginfo,train,test,train_target



def pipeline(iteration,C,gamma,random_seed):
    x_train, _x , y_train, _y = train_test_split(train_x,train_y,test_size=0.4,random_state=random_seed)
    print x_train.shape
    clf = SVC(C=C,kernel='rbf',gamma=gamma,probability=True,cache_size=7000,class_weight='balanced',verbose=True,random_state=random_seed)
    clf.fit(x_train,y_train)
    #predict test set
    pred = clf.predict_proba(test_x)
    test_result = pd.DataFrame(columns=["Idx","score"])
    test_result.Idx = test_Idx
    test_result.score = pred[:,1]
    test_result.to_csv('./test/svm_{0}.csv'.format(iteration),index=None)
    #predict val set
    pred = clf.predict_proba(val_x)
    val_result = pd.DataFrame(columns=["Idx","score"])
    val_result.Idx = val_Idx
    val_result.score = pred[:,1]
    val_result.to_csv('./val/svm_{0}.csv'.format(iteration),index=None)


if __name__ == "__main__":
    os.mkdir('test')
    os.mkdir('val')
    random_seed = range(2016,2046)
    C = [i/10.0 for i in range(10,40)]
    gamma = [i/1000.0 for i in range(1,31)]
    random.shuffle(random_seed)
    random.shuffle(C)
    random.shuffle(gamma)

    with open('params.pkl','w') as f:
        cPickle.dump((random_seed,C,gamma),f)

    for i in range(30):
        pipeline(i,C[i],gamma[i],random_seed[i])
        
    #mp version
    """
    pool = Pool(4)
    for i in range(30):
        pool.apply_async(pipeline,args=(i,C[i],gamma[i],random_seed[i]))
    pool.close()
    pool.join()
    """


    
