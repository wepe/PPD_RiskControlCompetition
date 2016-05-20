"""
calculate the mic of two result

"""


import pandas as pd
import numpy as np
from minepy import MINE


xgb_7844 = pd.read_csv('xgb_7844.csv')
svm_771 = pd.read_csv('svm_771.csv')
xgb_787 = pd.read_csv('xgb_787.csv')



fs = ['xgb_7844','svm_771','xgb_787']

res = []
res.append(pd.read_csv('xgb_7844.csv').score.values)
res.append(pd.read_csv('svm_771.csv').score.values)
res.append(pd.read_csv('xgb_787.csv').score.values)


cm = []
for i in range(3):
    tmp = []
    for j in range(3):
        m = MINE()
        m.compute_score(res[i], res[j])
        tmp.append(m.mic())
    cm.append(tmp)


import numpy as np
import matplotlib.pyplot as plt

def plot_confusion_matrix(cm, title, cmap=plt.cm.Blues):
    plt.imshow(cm, interpolation='nearest', cmap=cmap)
    plt.title(title)
    plt.colorbar()
    tick_marks = np.arange(3)
    plt.xticks(tick_marks, fs, rotation=45)
    plt.yticks(tick_marks, fs)
    plt.tight_layout()

plot_confusion_matrix(cm, title='mic')
plt.show()

