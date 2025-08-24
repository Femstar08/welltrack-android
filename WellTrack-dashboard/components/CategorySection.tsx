import { useState } from "react";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { Badge } from "./ui/badge";
import { ShoppingListItem } from "./ShoppingListItem";
import { ChevronDown, ChevronUp } from "lucide-react";
import { Category, ShoppingItem } from "./ShoppingList";

interface CategorySectionProps {
  category: Category;
  onToggleItem: (categoryId: string, itemId: string) => void;
}

export function CategorySection({ category, onToggleItem }: CategorySectionProps) {
  const [isOpen, setIsOpen] = useState(true);
  const completedItems = category.items.filter(item => item.completed);
  const pendingItems = category.items.filter(item => !item.completed);
  
  return (
    <Collapsible open={isOpen} onOpenChange={setIsOpen}>
      <CollapsibleTrigger className="w-full">
        <div className="flex items-center justify-between p-3 bg-card border border-border rounded-lg hover:bg-accent/50 transition-colors">
          <div className="flex items-center gap-3">
            <span className="text-xl">{category.icon}</span>
            <div className="text-left">
              <h3 className="font-medium text-card-foreground">{category.name}</h3>
              <p className="text-xs text-muted-foreground">
                {category.completed} of {category.items.length} items
              </p>
            </div>
          </div>
          
          <div className="flex items-center gap-2">
            {category.completed > 0 && (
              <Badge className="bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200">
                {category.completed} done
              </Badge>
            )}
            {pendingItems.length > 0 && (
              <Badge variant="secondary">
                {pendingItems.length} left
              </Badge>
            )}
            {isOpen ? (
              <ChevronUp className="w-4 h-4 text-muted-foreground" />
            ) : (
              <ChevronDown className="w-4 h-4 text-muted-foreground" />
            )}
          </div>
        </div>
      </CollapsibleTrigger>
      
      <CollapsibleContent className="mt-2 space-y-2">
        {/* Pending Items First */}
        {pendingItems.map((item) => (
          <ShoppingListItem
            key={item.id}
            item={item}
            onToggle={() => onToggleItem(category.id, item.id)}
          />
        ))}
        
        {/* Completed Items */}
        {completedItems.length > 0 && (
          <div className="space-y-2 mt-4">
            {completedItems.length > 0 && pendingItems.length > 0 && (
              <div className="flex items-center gap-2 px-3">
                <div className="flex-1 h-px bg-border"></div>
                <span className="text-xs text-muted-foreground px-2">Completed</span>
                <div className="flex-1 h-px bg-border"></div>
              </div>
            )}
            {completedItems.map((item) => (
              <ShoppingListItem
                key={item.id}
                item={item}
                onToggle={() => onToggleItem(category.id, item.id)}
              />
            ))}
          </div>
        )}
      </CollapsibleContent>
    </Collapsible>
  );
}