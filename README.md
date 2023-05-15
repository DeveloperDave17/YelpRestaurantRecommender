# YelpRestaurantRecommender

Achieves a combination of 3 completed assignments for CSC 365.

Assignment 1: This assignment asks you to create a similarity-based recommendation system using the online Yelp sample dataset. Get it, and (recommended) also the csv converter from the the Yelp github utilities.
* The program stores records from any of the data sets and choice of key. For this assignment, you can limit the data store size to 10000 records (ignoring the rest). You can choose to read data either as JSON or csv.
* Establish a similarity metric, that must include information based on custom frequency tables (as will be discussed in class), possibly weighted by or in conjunction with other attributes.
* Create a GUI that allows a user to indicate one entity, and displays two similar ones.

Assignment 2: This extends Assignment 1 using persistent data structures and additional similarity metrics. It requires two programs.

Loader:
* For each of at least 10,000 businesses, create a file-based structured representation containing everything needed for your similarity metric.
* Create a persistent block-based extensible hash table that maps businesses to their representation file names. You may (and are encouraged to) maintain a buffer cache to speed up IO.
* Traverse this map to pre-categorize (and somehow store) records into 5 to 10 clusters using k-means, k-mediods, or a similar metric as discussed in class and outlined in the course notes.

Application:
* Extend Assignment 1 to display a category (cluster) and most similar key from the above data structures.

Assignment 3:
* Extend Assignment 2 to record links from each business to its four geographically closest neighbors. (You can use the Haversine formula based on longitudes and latitudes.) As a connectivity check, report the number of disjoint sets (from arbitrary roots). Store persistently (possibly just in a Serialized file).
* Write a program (either GUI or web-based) that uses graph from step 1, allows a user to select any node, and displays the shortest (weighted using any similarity metric other than geographical distance) path between that node and the nearest (reachable) cluster center, using connected sets from step 1 to determine reachability.

