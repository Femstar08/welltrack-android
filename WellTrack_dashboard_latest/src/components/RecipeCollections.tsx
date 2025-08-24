import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Input } from "./ui/input";
import { 
  Plus,
  Edit,
  Trash2,
  Clock,
  Package,
  Heart,
  Star,
  Users,
  ChefHat,
  BookOpen,
  FolderPlus,
  Folder,
  Grid3X3
} from "lucide-react";
import { Recipe, RecipeCollection } from "./Recipes";

interface RecipeCollectionsProps {
  collections: RecipeCollection[];
  recipes: Recipe[];
  viewMode: 'grid' | 'list';
}

export function RecipeCollections({ collections, recipes, viewMode }: RecipeCollectionsProps) {
  const [selectedCollection, setSelectedCollection] = useState<RecipeCollection | null>(null);
  const [showCreateCollection, setShowCreateCollection] = useState(false);
  const [newCollectionName, setNewCollectionName] = useState('');

  const handleCreateCollection = () => {
    if (newCollectionName.trim()) {
      console.log('Create collection:', newCollectionName);
      setNewCollectionName('');
      setShowCreateCollection(false);
    }
  };

  const handleDeleteCollection = (collectionId: string) => {
    console.log('Delete collection:', collectionId);
  };

  const getCollectionRecipes = (collection: RecipeCollection) => {
    return recipes.filter(recipe => collection.recipeIds.includes(recipe.id));
  };

  const getCollectionIcon = (iconName: string) => {
    switch (iconName) {
      case 'Clock': return <Clock className="w-5 h-5" />;
      case 'Package': return <Package className="w-5 h-5" />;
      case 'Heart': return <Heart className="w-5 h-5" />;
      case 'Star': return <Star className="w-5 h-5" />;
      case 'ChefHat': return <ChefHat className="w-5 h-5" />;
      default: return <Folder className="w-5 h-5" />;
    }
  };

  const getCollectionColor = (color: string) => {
    switch (color) {
      case 'blue': return 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-200';
      case 'green': return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200';
      case 'purple': return 'bg-purple-100 text-purple-800 dark:bg-purple-950 dark:text-purple-200';
      case 'red': return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      case 'yellow': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 'orange': return 'bg-orange-100 text-orange-800 dark:bg-orange-950 dark:text-orange-200';
      default: return 'bg-gray-100 text-gray-800 dark:bg-gray-950 dark:text-gray-200';
    }
  };

  const renderStars = (rating: number) => {
    return (
      <div className="flex items-center gap-0.5">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star 
            key={star}
            className={`w-3 h-3 ${star <= rating ? 'text-yellow-500 fill-current' : 'text-muted-foreground'}`}
          />
        ))}
      </div>
    );
  };

  // If viewing a specific collection
  if (selectedCollection) {
    const collectionRecipes = getCollectionRecipes(selectedCollection);
    
    return (
      <div className="h-full overflow-y-auto p-4 space-y-4">
        {/* Collection Header */}
        <div className="flex items-center gap-3 pb-4 border-b border-border">
          <Button 
            variant="ghost" 
            size="sm" 
            onClick={() => setSelectedCollection(null)}
          >
            ← Back
          </Button>
          <div className={`p-2 rounded-lg ${getCollectionColor(selectedCollection.color)}`}>
            {getCollectionIcon(selectedCollection.icon)}
          </div>
          <div className="flex-1">
            <h2 className="font-semibold text-card-foreground">{selectedCollection.name}</h2>
            <p className="text-sm text-muted-foreground">{selectedCollection.description}</p>
          </div>
          <Badge variant="secondary">
            {collectionRecipes.length} recipes
          </Badge>
        </div>

        {/* Recipes in Collection */}
        {collectionRecipes.length === 0 ? (
          <div className="text-center py-12">
            <BookOpen className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
            <p className="text-muted-foreground mb-4">
              This collection is empty. Add some recipes to get started!
            </p>
            <Button>
              Browse Recipes
            </Button>
          </div>
        ) : (
          <div className={viewMode === 'grid' ? 'grid grid-cols-2 gap-3' : 'space-y-3'}>
            {collectionRecipes.map((recipe) => (
              <Card key={recipe.id} className="hover:bg-accent/50 transition-colors overflow-hidden">
                {viewMode === 'grid' ? (
                  <CardContent className="p-0">
                    <div className="relative aspect-[4/3]">
                      <img
                        src={recipe.image}
                        alt={recipe.title}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div className="p-3 space-y-2">
                      <h3 className="font-medium text-card-foreground line-clamp-2 text-sm">
                        {recipe.title}
                      </h3>
                      <div className="flex items-center justify-between text-xs text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <Clock className="w-3 h-3" />
                          {recipe.prepTime + recipe.cookTime}m
                        </div>
                        <div className="flex items-center gap-1">
                          <Users className="w-3 h-3" />
                          {recipe.servings}
                        </div>
                        {renderStars(recipe.rating)}
                      </div>
                      <Button size="sm" className="w-full text-xs h-8">
                        View Recipe
                      </Button>
                    </div>
                  </CardContent>
                ) : (
                  <CardContent className="p-4">
                    <div className="flex gap-4">
                      <div className="w-16 h-16 flex-shrink-0">
                        <img
                          src={recipe.image}
                          alt={recipe.title}
                          className="w-full h-full object-cover rounded-lg"
                        />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-medium text-card-foreground line-clamp-1">
                          {recipe.title}
                        </h3>
                        <p className="text-sm text-muted-foreground line-clamp-2">
                          {recipe.description}
                        </p>
                        <div className="flex items-center gap-4 text-sm text-muted-foreground mt-2">
                          <div className="flex items-center gap-1">
                            <Clock className="w-3 h-3" />
                            {recipe.prepTime + recipe.cookTime}m
                          </div>
                          <div className="flex items-center gap-1">
                            {renderStars(recipe.rating)}
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                )}
              </Card>
            ))}
          </div>
        )}
      </div>
    );
  }

  // Main collections view
  if (collections.length === 0) {
    return (
      <div className="h-full flex flex-col items-center justify-center p-8 text-center">
        <Folder className="w-16 h-16 text-muted-foreground mb-4" />
        <h3 className="text-lg font-semibold text-card-foreground mb-2">
          No Recipe Collections Yet
        </h3>
        <p className="text-muted-foreground mb-6 max-w-sm">
          Organize your recipes into collections like "Quick Dinners" or "Healthy Meals" 
          to find them easily later.
        </p>
        
        <Button 
          size="lg" 
          onClick={() => setShowCreateCollection(true)}
        >
          <FolderPlus className="w-5 h-5 mr-2" />
          Create Your First Collection
        </Button>
      </div>
    );
  }

  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h3 className="font-medium text-card-foreground">
          Recipe Collections ({collections.length})
        </h3>
        <Button size="sm" onClick={() => setShowCreateCollection(true)}>
          <Plus className="w-4 h-4 mr-1" />
          New Collection
        </Button>
      </div>

      {/* Collections Grid */}
      <div className="grid grid-cols-1 gap-3">
        {collections.map((collection) => {
          const collectionRecipes = getCollectionRecipes(collection);
          const totalTime = collectionRecipes.reduce((sum, recipe) => 
            sum + recipe.prepTime + recipe.cookTime, 0
          );
          const avgRating = collectionRecipes.length > 0 ? 
            collectionRecipes.reduce((sum, recipe) => sum + recipe.rating, 0) / collectionRecipes.length : 0;

          return (
            <Card 
              key={collection.id} 
              className="hover:bg-accent/50 transition-colors cursor-pointer"
              onClick={() => setSelectedCollection(collection)}
            >
              <CardContent className="p-4">
                <div className="flex items-center gap-4">
                  {/* Collection Icon */}
                  <div className={`p-3 rounded-lg ${getCollectionColor(collection.color)}`}>
                    {getCollectionIcon(collection.icon)}
                  </div>

                  {/* Collection Info */}
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <h3 className="font-medium text-card-foreground">
                        {collection.name}
                      </h3>
                      {collection.isDefault && (
                        <Badge variant="secondary" className="text-xs">
                          Default
                        </Badge>
                      )}
                    </div>
                    <p className="text-sm text-muted-foreground mb-2">
                      {collection.description}
                    </p>
                    
                    {/* Collection Stats */}
                    <div className="flex items-center gap-4 text-xs text-muted-foreground">
                      <span>{collectionRecipes.length} recipes</span>
                      {collectionRecipes.length > 0 && (
                        <>
                          <span>Avg: {Math.round(totalTime / collectionRecipes.length)}m</span>
                          <div className="flex items-center gap-1">
                            {renderStars(avgRating)}
                            <span>({avgRating.toFixed(1)})</span>
                          </div>
                        </>
                      )}
                    </div>
                  </div>

                  {/* Recipe Preview Images */}
                  <div className="flex -space-x-2">
                    {collectionRecipes.slice(0, 3).map((recipe, index) => (
                      <div 
                        key={recipe.id}
                        className={`w-10 h-10 rounded-lg border-2 border-background overflow-hidden ${
                          index > 0 ? 'z-10' : 'z-20'
                        }`}
                      >
                        <img
                          src={recipe.image}
                          alt={recipe.title}
                          className="w-full h-full object-cover"
                        />
                      </div>
                    ))}
                    {collectionRecipes.length > 3 && (
                      <div className="w-10 h-10 rounded-lg border-2 border-background bg-muted flex items-center justify-center text-xs font-medium text-muted-foreground">
                        +{collectionRecipes.length - 3}
                      </div>
                    )}
                  </div>

                  {/* Action Buttons */}
                  <div className="flex items-center gap-1">
                    {!collection.isDefault && (
                      <>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0"
                          onClick={(e) => {
                            e.stopPropagation();
                            console.log('Edit collection:', collection.id);
                          }}
                        >
                          <Edit className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          className="h-8 w-8 p-0 text-red-500 hover:text-red-700"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteCollection(collection.id);
                          }}
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Create Collection Modal */}
      {showCreateCollection && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <Card className="w-full max-w-md">
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                Create New Collection
                <Button 
                  variant="ghost" 
                  size="sm" 
                  onClick={() => setShowCreateCollection(false)}
                >
                  ×
                </Button>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <label className="text-sm font-medium text-card-foreground mb-2 block">
                  Collection Name
                </label>
                <Input
                  placeholder="e.g., Quick Weeknight Dinners"
                  value={newCollectionName}
                  onChange={(e) => setNewCollectionName(e.target.value)}
                />
              </div>
              
              <div className="flex gap-2">
                <Button 
                  className="flex-1"
                  onClick={handleCreateCollection}
                  disabled={!newCollectionName.trim()}
                >
                  Create Collection
                </Button>
                <Button 
                  variant="outline" 
                  onClick={() => setShowCreateCollection(false)}
                >
                  Cancel
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}