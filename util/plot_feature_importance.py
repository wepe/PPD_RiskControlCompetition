import pandas as pd
import matplotlib.pylab as plt

fs = pd.read_csv('tmp.csv')

fs.index = fs.feature
fs.drop('feature',axis=1,inplace=True)
fs.ix[range(19,0,-1)].plot.barh(title='Feature Importance')
plt.show()
