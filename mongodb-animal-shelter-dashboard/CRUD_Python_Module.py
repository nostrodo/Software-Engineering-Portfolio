# Example Python Code to Insert a Document 

from pymongo import MongoClient 
from bson.objectid import ObjectId 

class AnimalShelter(object): 
    """ CRUD operations for Animal collection in MongoDB """ 

    # udated Dash login
    def __init__(self, username, password,
                 host='localhost', port=27017, db='aac', col='animals'):
        uri = f"mongodb://{username}:{password}@{host}:{port}"
        self.client = MongoClient(uri)
        self.database = self.client[db]
        self.collection = self.database[col]
            
    # Complete this create method to implement the C in CRUD. 
    def create(self, data):
        if data is not None: 
            self.collection.insert_one(data)
            return True
        else: 
            return False

    # Create method to implement the R in CRUD.
    def read(self, query):
        if query is None:
            return []

        try:
            cursor = self.collection.find(query)
            return list(cursor)
        except Exception:
            return []
        
    # Create method to implement the U in CRUD.
    def update(self, query, update_data):
        if query is None or update_data is None:
            return 0

        try:
            result = self.collection.update_many(query, {"$set": update_data})
            return result.modified_count
        except Exception:
            return 0

    # Create method to implement the D in CRUD.
    def delete(self, query):
        if query is None:
            return 0

        try:
            result = self.collection.delete_many(query)
            return result.deleted_count
        except Exception:
            return 0

    
    # Retaining the original login code info for safety   
    # def __init__(self):
            # Initializing the MongoClient. This helps to access the MongoDB 
        # databases and collections. This is hard-wired to use the aac 
        # database, the animals collection, and the aac user. 
        # 
        # You must edit the password below for your environment. 
        # 
        # Connection Variables 
        # 
        # USER = 'YOUR_USERNAME' 
        # PASS = 'YOUR_PASSWORD' 
        # HOST = 'localhost' 
        # PORT = 27017 
        # DB = 'aac' 
        # COL = 'animals' 
        #
        # Initialize Connection 
        # 
        # self.client = MongoClient('mongodb://%s:%s@%s:%d' % (USER,PASS,HOST,PORT)) 
        # self.database = self.client['%s' % (DB)] 
        # self.collection = self.database['%s' % (COL)] 