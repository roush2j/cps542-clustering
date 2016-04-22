
# coding: utf-8

# In[34]:

import matplotlib.pyplot as plt


# In[35]:

get_ipython().magic('matplotlib inline')


# In[41]:

import pandas as pd
import numpy as np


# In[8]:

ddata = pd.read_table("Downloads/retada/gaussiangen.csv")


# In[9]:

ddata.head()


# In[ ]:




# In[ ]:




# In[70]:

x0=list(ddata[ddata.DBSCANID == 0]['x'])
y0=list(ddata[ddata.DBSCANID == 0]['y'])
x1=list(ddata[ddata.DBSCANID == 1]['x'])
y1=list(ddata[ddata.DBSCANID == 1]['y'])
x2=list(ddata[ddata.DBSCANID == 2]['x'])
y2=list(ddata[ddata.DBSCANID == 2]['y'])
x3=list(ddata[ddata.DBSCANID == 3]['x'])
y3=list(ddata[ddata.DBSCANID == 3]['y'])
x4=list(ddata[ddata.DBSCANID == 4]['x'])
y4=list(ddata[ddata.DBSCANID == 4]['y'])
x5=list(ddata[ddata.DBSCANID == 5]['x'])
y5=list(ddata[ddata.DBSCANID == 5]['y'])

colors = np.random.rand(50)
# area = np.pi * (15 * np.random.rand(50))**2  # 0 to 15 point radiuses

plt.scatter(x1, y1,color='blue', alpha=0.5)
plt.scatter(x2, y2, color='red', alpha=0.5)
plt.scatter(x3,y3,color='green',alpha=0.5)
plt.scatter(x4,y4,color='yellow',alpha=0.5)
plt.scatter(x5,y5,color='black',alpha=0.5)
plt.scatter(x0,y0,color='cyan',alpha=0.5)
plt.show()


# In[72]:

x0=list(ddata[ddata.KmeansID == 0]['x'])
y0=list(ddata[ddata.KmeansID == 0]['y'])
x1=list(ddata[ddata.KmeansID == 1]['x'])
y1=list(ddata[ddata.KmeansID == 1]['y'])
x2=list(ddata[ddata.KmeansID == 2]['x'])
y2=list(ddata[ddata.KmeansID == 2]['y'])
x3=list(ddata[ddata.KmeansID == 3]['x'])
y3=list(ddata[ddata.KmeansID == 3]['y'])
x4=list(ddata[ddata.KmeansID == 4]['x'])
y4=list(ddata[ddata.KmeansID == 4]['y'])
x5=list(ddata[ddata.KmeansID == 5]['x'])
y5=list(ddata[ddata.KmeansID == 5]['y'])

colors = np.random.rand(50)
# area = np.pi * (15 * np.random.rand(50))**2  # 0 to 15 point radiuses

plt.scatter(x1, y1,color='blue', alpha=0.5)
plt.scatter(x2, y2, color='red', alpha=0.5)
plt.scatter(x3,y3,color='green',alpha=0.5)
plt.scatter(x4,y4,color='yellow',alpha=0.5)
plt.scatter(x5,y5,color='black',alpha=0.5)
plt.scatter(x0,y0,color='cyan',alpha=0.5)
plt.show()


# In[73]:

x0=list(ddata[ddata.GeneratedID == 0]['x'])
y0=list(ddata[ddata.GeneratedID == 0]['y'])
x1=list(ddata[ddata.GeneratedID == 1]['x'])
y1=list(ddata[ddata.GeneratedID == 1]['y'])
x2=list(ddata[ddata.GeneratedID == 2]['x'])
y2=list(ddata[ddata.GeneratedID == 2]['y'])
x3=list(ddata[ddata.GeneratedID == 3]['x'])
y3=list(ddata[ddata.GeneratedID == 3]['y'])
x4=list(ddata[ddata.GeneratedID == 4]['x'])
y4=list(ddata[ddata.GeneratedID == 4]['y'])
x5=list(ddata[ddata.GeneratedID == 5]['x'])
y5=list(ddata[ddata.GeneratedID == 5]['y'])

colors = np.random.rand(50)
# area = np.pi * (15 * np.random.rand(50))**2  # 0 to 15 point radiuses

plt.scatter(x1, y1,color='blue', alpha=0.5)
plt.scatter(x2, y2, color='red', alpha=0.5)
plt.scatter(x3,y3,color='green',alpha=0.5)
plt.scatter(x4,y4,color='yellow',alpha=0.5)
plt.scatter(x5,y5,color='black',alpha=0.5)
plt.scatter(x0,y0,color='cyan',alpha=0.5)
plt.show()


# In[ ]:



