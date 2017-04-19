try:
  from setuptools import setup
except ImportError:
  from distutils.core import setup

#print find_packages('commons', exclude=['*.txt'])

config = {
  'description': 'Train and Predict using Linear Models',
  'author': 'People Pattern',
  'author_email': 'datasci@peoplepattern.com',
  'version': '0.2',
  'install_requires': ['nose','mmh3'],
  'packages': ['lemkit', 'lemkit.predict', 'lemkit.train'],
  'name': 'lemkit'
}

setup(**config)
