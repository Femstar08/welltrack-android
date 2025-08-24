import { useState } from "react";
import { ShoppingListHeader } from "./ShoppingListHeader";
import { ListManagementBar } from "./ListManagementBar";
import { ProgressIndicator } from "./ProgressIndicator";
import { CategorySection } from "./CategorySection";
import { SmartFeaturesPanel } from "./SmartFeaturesPanel";
import { FloatingActionButton } from "./FloatingActionButton";

export interface ShoppingItem {
  id: string;
  name: string;
  brand?: string;
  quantity: number;
  unit: string;
  estimatedPrice?: number;
  source: 'meal-plan' | 'pantry' | 'manual';
  priority: 'essential' | 'preferred' | 'optional';
  completed: boolean;
  recipeContext?: string;
  category: string;
  notes?: string;
}

export interface Category {
  id: string;
  name: string;
  icon: string;
  items: ShoppingItem[];
  completed: number;
}

export function ShoppingList() {
  const [activeList, setActiveList] = useState("weekly-groceries");
  const [showSmartFeatures, setShowSmartFeatures] = useState(false);
  const [autoAddPantry, setAutoAddPantry] = useState(true);

  // Mock data for demonstration
  const [categories, setCategories] = useState<Category[]>([
    {
      id: 'produce',
      name: 'Fresh Produce',
      icon: '🥬',
      completed: 2,
      items: [
        {
          id: '1',
          name: 'Baby Spinach',
          quantity: 1,
          unit: 'bag',
          estimatedPrice: 2.99,
          source: 'meal-plan',
          priority: 'essential',
          completed: true,
          recipeContext: 'for Quinoa Buddha Bowl',
          category: 'produce'
        },
        {
          id: '2',
          name: 'Avocados',
          quantity: 2,
          unit: 'pieces',
          estimatedPrice: 1.50,
          source: 'meal-plan',
          priority: 'essential',
          completed: true,
          recipeContext: 'for Avocado Toast',
          category: 'produce'
        },
        {
          id: '3',
          name: 'Bell Peppers',
          brand: 'Organic',
          quantity: 3,
          unit: 'pieces',
          estimatedPrice: 4.99,
          source: 'meal-plan',
          priority: 'essential',
          completed: false,
          recipeContext: 'for Chicken Stir Fry',
          category: 'produce'
        },
        {
          id: '4',
          name: 'Bananas',
          quantity: 1,
          unit: 'bunch',
          estimatedPrice: 2.49,
          source: 'pantry',
          priority: 'preferred',
          completed: false,
          category: 'produce'
        }
      ]
    },
    {
      id: 'dairy',
      name: 'Dairy & Eggs',
      icon: '🥛',
      completed: 1,
      items: [
        {
          id: '5',
          name: 'Greek Yogurt',
          brand: 'Chobani',
          quantity: 2,
          unit: 'containers',
          estimatedPrice: 5.98,
          source: 'meal-plan',
          priority: 'essential',
          completed: false,
          recipeContext: 'for Greek Yogurt Parfait',
          category: 'dairy'
        },
        {
          id: '6',
          name: 'Eggs',
          brand: 'Free Range',
          quantity: 1,
          unit: 'dozen',
          estimatedPrice: 4.99,
          source: 'pantry',
          priority: 'essential',
          completed: true,
          category: 'dairy'
        }
      ]
    },
    {
      id: 'meat',
      name: 'Meat & Seafood',
      icon: '🐟',
      completed: 0,
      items: [
        {
          id: '7',
          name: 'Salmon Fillets',
          quantity: 2,
          unit: 'pieces',
          estimatedPrice: 12.99,
          source: 'meal-plan',
          priority: 'essential',
          completed: false,
          recipeContext: 'for Grilled Salmon with Veggies',
          category: 'meat'
        },
        {
          id: '8',
          name: 'Chicken Breast',
          quantity: 1,
          unit: 'lb',
          estimatedPrice: 6.99,
          source: 'meal-plan',
          priority: 'essential',
          completed: false,
          recipeContext: 'for Chicken Stir Fry',
          category: 'meat'
        }
      ]
    },
    {
      id: 'pantry',
      name: 'Pantry Staples',
      icon: '🥫',
      completed: 1,
      items: [
        {
          id: '9',
          name: 'Quinoa',
          brand: 'Organic',
          quantity: 1,
          unit: 'bag',
          estimatedPrice: 7.99,
          source: 'pantry',
          priority: 'essential',
          completed: true,
          category: 'pantry'
        },
        {
          id: '10',
          name: 'Olive Oil',
          brand: 'Extra Virgin',
          quantity: 1,
          unit: 'bottle',
          estimatedPrice: 8.99,
          source: 'pantry',
          priority: 'preferred',
          completed: false,
          category: 'pantry'
        }
      ]
    },
    {
      id: 'supplements',
      name: 'Health & Supplements',
      icon: '💊',
      completed: 0,
      items: [
        {
          id: '11',
          name: 'Vitamin D3',
          brand: 'Nature Made',
          quantity: 1,
          unit: 'bottle',
          estimatedPrice: 12.99,
          source: 'manual',
          priority: 'optional',
          completed: false,
          category: 'supplements'
        }
      ]
    }
  ]);

  const totalItems = categories.reduce((sum, cat) => sum + cat.items.length, 0);
  const completedItems = categories.reduce((sum, cat) => sum + cat.completed, 0);
  const totalCost = categories.reduce((sum, cat) => 
    sum + cat.items.reduce((catSum, item) => 
      catSum + (item.estimatedPrice || 0), 0
    ), 0
  );

  const handleToggleItem = (categoryId: string, itemId: string) => {
    setCategories(prev => prev.map(category => {
      if (category.id === categoryId) {
        const updatedItems = category.items.map(item => 
          item.id === itemId ? { ...item, completed: !item.completed } : item
        );
        const newCompleted = updatedItems.filter(item => item.completed).length;
        return { ...category, items: updatedItems, completed: newCompleted };
      }
      return category;
    }));
  };

  const handleAutoGenerate = () => {
    // In a real app, this would fetch from meal planner
    console.log('Auto-generating shopping list from meal plans...');
  };

  return (
    <div className="flex flex-col h-full relative">
      {/* Header */}
      <ShoppingListHeader 
        activeList={activeList}
        onListChange={setActiveList}
        totalItems={totalItems}
        estimatedCost={totalCost}
      />

      {/* List Management Bar */}
      <ListManagementBar
        onAutoGenerate={handleAutoGenerate}
        autoAddPantry={autoAddPantry}
        onAutoAddPantryChange={setAutoAddPantry}
        onShowSmartFeatures={() => setShowSmartFeatures(!showSmartFeatures)}
        showSmartFeatures={showSmartFeatures}
      />

      {/* Progress Indicator */}
      <ProgressIndicator 
        completed={completedItems}
        total={totalItems}
      />

      {/* Main Content */}
      <div className="flex-1 overflow-hidden">
        <div className="flex h-full">
          {/* Shopping List */}
          <div className="flex-1 overflow-y-auto">
            <div className="p-4 space-y-4">
              {categories.map((category) => (
                <CategorySection
                  key={category.id}
                  category={category}
                  onToggleItem={handleToggleItem}
                />
              ))}
            </div>
          </div>

          {/* Smart Features Panel */}
          {showSmartFeatures && (
            <div className="hidden md:block w-80 border-l border-border overflow-y-auto">
              <SmartFeaturesPanel />
            </div>
          )}
        </div>

        {/* Mobile Smart Features - Show as overlay on mobile */}
        {showSmartFeatures && (
          <div className="md:hidden fixed inset-0 z-50 bg-background">
            <div className="flex flex-col h-full">
              <div className="p-4 border-b border-border">
                <button
                  onClick={() => setShowSmartFeatures(false)}
                  className="text-sm text-muted-foreground"
                >
                  ← Back to Shopping List
                </button>
              </div>
              <div className="flex-1 overflow-y-auto">
                <SmartFeaturesPanel />
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Floating Action Button */}
      <FloatingActionButton />
    </div>
  );
}