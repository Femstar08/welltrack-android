import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Switch } from "./ui/switch";
import { Input } from "./ui/input";
import { Textarea } from "./ui/textarea";
import { 
  Plus,
  Edit,
  Trash2,
  AlertTriangle,
  Heart,
  Leaf,
  ChefHat,
  Flame,
  Apple,
  Search,
  X
} from "lucide-react";
import { DietaryRestriction, UserProfile } from "./Profile";

interface DietaryPreferencesProps {
  dietaryRestrictions: DietaryRestriction[];
  user: UserProfile;
}

export function DietaryPreferences({ dietaryRestrictions, user }: DietaryPreferencesProps) {
  const [showAddRestriction, setShowAddRestriction] = useState(false);
  const [showCustomIngredients, setShowCustomIngredients] = useState(false);
  const [newRestrictionName, setNewRestrictionName] = useState('');
  const [selectedCuisines, setSelectedCuisines] = useState<string[]>(['Italian', 'Mediterranean', 'Asian']);
  const [likedIngredients, setLikedIngredients] = useState<string[]>(['chicken', 'broccoli', 'quinoa', 'salmon', 'avocado']);
  const [dislikedIngredients, setDislikedIngredients] = useState<string[]>(['mushrooms', 'liver', 'blue cheese']);
  const [spiceLevel, setSpiceLevel] = useState(2); // 0-4 scale
  const [cookingSkill, setCookingSkill] = useState('intermediate');

  const commonRestrictions = [
    { name: 'Vegetarian', type: 'lifestyle', icon: '🌱' },
    { name: 'Vegan', type: 'lifestyle', icon: '🥬' },
    { name: 'Pescatarian', type: 'lifestyle', icon: '🐟' },
    { name: 'Gluten-Free', type: 'intolerance', icon: '🌾' },
    { name: 'Dairy-Free', type: 'intolerance', icon: '🥛' },
    { name: 'Nut-Free', type: 'allergy', icon: '🥜' },
    { name: 'Keto', type: 'lifestyle', icon: '🥓' },
    { name: 'Low-Carb', type: 'lifestyle', icon: '🥗' },
    { name: 'High-Protein', type: 'lifestyle', icon: '💪' },
    { name: 'Paleo', type: 'lifestyle', icon: '🥩' },
    { name: 'Low-Sodium', type: 'preference', icon: '🧂' },
    { name: 'Sugar-Free', type: 'preference', icon: '🍯' }
  ];

  const cuisineTypes = [
    'Italian', 'Chinese', 'Japanese', 'Mexican', 'Indian', 'Thai', 'Mediterranean', 
    'French', 'American', 'Korean', 'Vietnamese', 'Middle Eastern', 'Greek', 
    'Spanish', 'German', 'British', 'Moroccan', 'Lebanese', 'Turkish', 'Ethiopian'
  ];

  const getRestrictionIcon = (type: string) => {
    switch (type) {
      case 'allergy': return <AlertTriangle className="w-4 h-4 text-red-500" />;
      case 'intolerance': return <Heart className="w-4 h-4 text-orange-500" />;
      case 'lifestyle': return <Leaf className="w-4 h-4 text-green-500" />;
      case 'preference': return <Apple className="w-4 h-4 text-blue-500" />;
      default: return <Apple className="w-4 h-4 text-gray-500" />;
    }
  };

  const getRestrictionColor = (type: string) => {
    switch (type) {
      case 'allergy': return 'bg-red-100 text-red-800 border-red-200 dark:bg-red-950 dark:text-red-200 dark:border-red-800';
      case 'intolerance': return 'bg-orange-100 text-orange-800 border-orange-200 dark:bg-orange-950 dark:text-orange-200 dark:border-orange-800';
      case 'lifestyle': return 'bg-green-100 text-green-800 border-green-200 dark:bg-green-950 dark:text-green-200 dark:border-green-800';
      case 'preference': return 'bg-blue-100 text-blue-800 border-blue-200 dark:bg-blue-950 dark:text-blue-200 dark:border-blue-800';
      default: return 'bg-gray-100 text-gray-800 border-gray-200 dark:bg-gray-950 dark:text-gray-200 dark:border-gray-800';
    }
  };

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'severe': return 'bg-red-500';
      case 'moderate': return 'bg-yellow-500';
      case 'mild': return 'bg-green-500';
      default: return 'bg-gray-500';
    }
  };

  const handleAddRestriction = (restrictionName: string, type: string) => {
    console.log('Add restriction:', restrictionName, type);
    setShowAddRestriction(false);
    setNewRestrictionName('');
  };

  const handleToggleRestriction = (restrictionId: string) => {
    console.log('Toggle restriction:', restrictionId);
  };

  const handleRemoveRestriction = (restrictionId: string) => {
    console.log('Remove restriction:', restrictionId);
  };

  const toggleCuisine = (cuisine: string) => {
    setSelectedCuisines(prev => 
      prev.includes(cuisine) 
        ? prev.filter(c => c !== cuisine)
        : [...prev, cuisine]
    );
  };

  const addIngredient = (ingredient: string, type: 'liked' | 'disliked') => {
    if (type === 'liked') {
      setLikedIngredients(prev => [...prev, ingredient]);
    } else {
      setDislikedIngredients(prev => [...prev, ingredient]);
    }
  };

  const removeIngredient = (ingredient: string, type: 'liked' | 'disliked') => {
    if (type === 'liked') {
      setLikedIngredients(prev => prev.filter(i => i !== ingredient));
    } else {
      setDislikedIngredients(prev => prev.filter(i => i !== ingredient));
    }
  };

  const spiceLevels = ['None', 'Mild', 'Medium', 'Hot', 'Extra Hot'];

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* Dietary Restrictions */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-orange-500" />
              Dietary Restrictions
            </div>
            <Button size="sm" onClick={() => setShowAddRestriction(true)}>
              <Plus className="w-4 h-4 mr-1" />
              Add
            </Button>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Active Restrictions */}
          <div className="space-y-3">
            {dietaryRestrictions.filter(r => r.isActive).map((restriction) => (
              <Card key={restriction.id} className={`border ${getRestrictionColor(restriction.type)}`}>
                <CardContent className="p-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-3">
                      {getRestrictionIcon(restriction.type)}
                      <div>
                        <h3 className="font-medium">{restriction.name}</h3>
                        <div className="flex items-center gap-2 mt-1">
                          <Badge variant="secondary" className="text-xs">
                            {restriction.type}
                          </Badge>
                          <div className={`w-2 h-2 rounded-full ${getSeverityColor(restriction.severity)}`} />
                          <span className="text-xs text-muted-foreground capitalize">
                            {restriction.severity}
                          </span>
                        </div>
                      </div>
                    </div>
                    
                    <div className="flex items-center gap-2">
                      <Switch 
                        checked={restriction.isActive}
                        onCheckedChange={() => handleToggleRestriction(restriction.id)}
                      />
                      <Button 
                        variant="ghost" 
                        size="sm" 
                        className="h-8 w-8 p-0 text-red-500 hover:text-red-700"
                        onClick={() => handleRemoveRestriction(restriction.id)}
                      >
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>

          {/* Add New Restriction */}
          {showAddRestriction && (
            <Card className="border-dashed border-2">
              <CardContent className="p-4 space-y-4">
                <div className="grid grid-cols-2 gap-2">
                  {commonRestrictions.map((restriction) => (
                    <Button
                      key={restriction.name}
                      variant="outline"
                      size="sm"
                      className="justify-start gap-2"
                      onClick={() => handleAddRestriction(restriction.name, restriction.type)}
                    >
                      <span>{restriction.icon}</span>
                      {restriction.name}
                    </Button>
                  ))}
                </div>
                
                <div className="space-y-2">
                  <Input
                    placeholder="Custom restriction name..."
                    value={newRestrictionName}
                    onChange={(e) => setNewRestrictionName(e.target.value)}
                  />
                  <div className="flex gap-2">
                    <Button 
                      size="sm" 
                      disabled={!newRestrictionName.trim()}
                      onClick={() => handleAddRestriction(newRestrictionName, 'preference')}
                    >
                      Add Custom
                    </Button>
                    <Button 
                      variant="outline" 
                      size="sm"
                      onClick={() => setShowAddRestriction(false)}
                    >
                      Cancel
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          )}
        </CardContent>
      </Card>

      {/* Food Preferences */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Heart className="w-5 h-5 text-red-500" />
            Food Preferences
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Liked Ingredients */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="font-medium text-card-foreground">Favorite Ingredients</h3>
              <Button variant="outline" size="sm">
                <Plus className="w-4 h-4 mr-1" />
                Add
              </Button>
            </div>
            <div className="flex flex-wrap gap-2">
              {likedIngredients.map((ingredient) => (
                <Badge 
                  key={ingredient} 
                  className="bg-green-100 text-green-800 dark:bg-green-950 hover:bg-green-200 dark:hover:bg-green-900 cursor-pointer"
                  onClick={() => removeIngredient(ingredient, 'liked')}
                >
                  {ingredient}
                  <X className="w-3 h-3 ml-1" />
                </Badge>
              ))}
            </div>
          </div>

          {/* Disliked Ingredients */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h3 className="font-medium text-card-foreground">Avoid These Ingredients</h3>
              <Button variant="outline" size="sm">
                <Plus className="w-4 h-4 mr-1" />
                Add
              </Button>
            </div>
            <div className="flex flex-wrap gap-2">
              {dislikedIngredients.map((ingredient) => (
                <Badge 
                  key={ingredient} 
                  className="bg-red-100 text-red-800 dark:bg-red-950 hover:bg-red-200 dark:hover:bg-red-900 cursor-pointer"
                  onClick={() => removeIngredient(ingredient, 'disliked')}
                >
                  {ingredient}
                  <X className="w-3 h-3 ml-1" />
                </Badge>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Cuisine Preferences */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <ChefHat className="w-5 h-5 text-purple-500" />
            Cuisine Preferences
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            Select your favorite cuisines to get personalized recipe recommendations.
          </p>
          
          <div className="grid grid-cols-3 gap-2">
            {cuisineTypes.map((cuisine) => (
              <Button
                key={cuisine}
                variant={selectedCuisines.includes(cuisine) ? "default" : "outline"}
                size="sm"
                className="justify-start"
                onClick={() => toggleCuisine(cuisine)}
              >
                {cuisine}
              </Button>
            ))}
          </div>
          
          <p className="text-xs text-muted-foreground">
            Selected: {selectedCuisines.length} cuisines
          </p>
        </CardContent>
      </Card>

      {/* Cooking Preferences */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Flame className="w-5 h-5 text-orange-500" />
            Cooking Preferences
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-6">
          {/* Spice Level */}
          <div className="space-y-3">
            <h3 className="font-medium text-card-foreground">Spice Tolerance</h3>
            <div className="flex items-center gap-4">
              <div className="flex-1">
                <div className="flex justify-between mb-2">
                  {spiceLevels.map((level, index) => (
                    <span 
                      key={level}
                      className={`text-xs ${index === spiceLevel ? 'font-medium text-card-foreground' : 'text-muted-foreground'}`}
                    >
                      {level}
                    </span>
                  ))}
                </div>
                <div className="flex gap-1">
                  {spiceLevels.map((_, index) => (
                    <Button
                      key={index}
                      variant="ghost"
                      size="sm"
                      className={`flex-1 h-8 p-0 ${
                        index <= spiceLevel 
                          ? 'bg-red-500 text-white hover:bg-red-600' 
                          : 'bg-muted hover:bg-muted/80'
                      }`}
                      onClick={() => setSpiceLevel(index)}
                    >
                      <Flame className="w-3 h-3" />
                    </Button>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Cooking Skill Level */}
          <div className="space-y-3">
            <h3 className="font-medium text-card-foreground">Cooking Skill Level</h3>
            <div className="grid grid-cols-3 gap-2">
              {['beginner', 'intermediate', 'advanced'].map((skill) => (
                <Button
                  key={skill}
                  variant={cookingSkill === skill ? "default" : "outline"}
                  size="sm"
                  className="capitalize"
                  onClick={() => setCookingSkill(skill)}
                >
                  {skill}
                </Button>
              ))}
            </div>
          </div>

          {/* Meal Complexity */}
          <div className="space-y-3">
            <h3 className="font-medium text-card-foreground">Preferred Meal Complexity</h3>
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm">Max Cook Time</span>
                  <span className="text-sm font-medium">45 minutes</span>
                </div>
                <div className="w-full bg-muted rounded-full h-2">
                  <div className="bg-blue-500 h-2 rounded-full" style={{ width: '60%' }} />
                </div>
              </div>
              
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <span className="text-sm">Max Ingredients</span>
                  <span className="text-sm font-medium">12 items</span>
                </div>
                <div className="w-full bg-muted rounded-full h-2">
                  <div className="bg-green-500 h-2 rounded-full" style={{ width: '70%' }} />
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Special Notes */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Edit className="w-5 h-5 text-blue-500" />
            Additional Notes
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Textarea
            placeholder="Any additional dietary preferences, allergies, or cooking notes you'd like to add..."
            className="min-h-[100px]"
          />
          <p className="text-xs text-muted-foreground mt-2">
            These notes will help personalize your recipe recommendations and meal plans.
          </p>
        </CardContent>
      </Card>
    </div>
  );
}