import pandas as pd
import matplotlib.pyplot as plt
from datetime import date

train = pd.read_csv('../data/train/train_master.csv',encoding='gb18030')
train_1 = train[train.target==1]
temp = train_1[['ListingInfo','target']].groupby('ListingInfo').agg('sum')*2
temp = temp.rename(columns={'target':'count_1'})
temp['date'] = temp.index
temp.date = temp.date.apply(lambda x:(date(int(x.split('-')[0]),int(x.split('-')[1]),int(x.split('-')[2]))-date(2013,11,1)).days)
temp = temp.sort(columns='date')


ax = temp.plot(x='date',y='count_1',title="train set")

train_0 = train[train.target==0]
train_0.target = [1 for _ in range(len(train_0))]
temp_0 = train_0[['ListingInfo','target']].groupby('ListingInfo').agg('sum')
temp_0 = temp_0.rename(columns={'target':'count_0'})
temp_0['date'] = temp_0.index
temp_0.date = temp_0.date.apply(lambda x:(date(int(x.split('-')[0]),int(x.split('-')[1]),int(x.split('-')[2]))-date(2013,11,1)).days)
temp_0 = temp_0.sort(columns='date')

temp_0.plot(x='date',y='count_0',ax=ax)

plt.xlabel('Date(20131101~20141109)')
plt.ylabel('count')

"""

test = pd.read_csv('../data/test/Kesci_Master_9w_gbk_1_test_set.csv',encoding='gb18030')
test['count_test'] = [8 for _ in range(len(test))]
temp = test[['ListingInfo','count_test']].groupby('ListingInfo').agg('sum')
temp['date'] = temp.index
temp.date = temp.date.apply(lambda x:(date(int(x.split('/')[2]),int(x.split('/')[1]),int(x.split('/')[0]))-date(2013,11,1)).days)
temp = temp.sort(columns='date')

temp.plot(x='date',y='count_test',ax=ax)
"""

plt.show()
plt.close()
