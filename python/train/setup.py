try:
  from setuptools import setup
except ImportError:
  from distutils.core import setup

#print find_packages('commons', exclude=['*.txt'])

config = {
  'description': 'Train Using Lemkit Model Files',
  'author': 'People Pattern',
  'author_email': 'datasci@peoplepattern.com',
  'version': '0.1',
  'install_requires': ['nose','mmh3', 'scipy', 'numpy', 'scikit-learn'],
  'packages': ['lemkit_train'],
  'scripts': ['bin/commons-predict'],
  'name': 'lemkit_train'
}

setup(**config)
