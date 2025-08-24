import { Button } from "./ui/button";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { ChevronDown, Share, ShoppingBag, DollarSign } from "lucide-react";

interface ShoppingListHeaderProps {
  activeList: string;
  onListChange: (listId: string) => void;
  totalItems: number;
  estimatedCost: number;
}

export function ShoppingListHeader({ 
  activeList, 
  onListChange, 
  totalItems, 
  estimatedCost 
}: ShoppingListHeaderProps) {
  const shoppingLists = [
    { id: "weekly-groceries", name: "Weekly Groceries", date: "Nov 18-24" },
    { id: "meal-prep", name: "Meal Prep Essentials", date: "Nov 20" },
    { id: "pantry-restock", name: "Pantry Restock", date: "Nov 22" },
    { id: "household", name: "Household Items", date: "Nov 25" }
  ];

  const currentList = shoppingLists.find(list => list.id === activeList) || shoppingLists[0];

  return (
    <div className="bg-card border-b border-border px-4 py-3">
      <div className="flex items-center justify-between mb-3">
        <h1 className="text-xl font-semibold text-card-foreground">Shopping List</h1>
        <Button variant="outline" size="sm" className="gap-2">
          <Share className="w-4 h-4" />
          Share
        </Button>
      </div>

      {/* Active List Selector */}
      <div className="flex items-center justify-between mb-3">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="h-auto p-0 justify-start">
              <div className="text-left">
                <div className="flex items-center gap-2">
                  <ShoppingBag className="w-4 h-4 text-green-600" />
                  <span className="font-medium text-card-foreground">{currentList.name}</span>
                  <ChevronDown className="w-4 h-4 text-muted-foreground" />
                </div>
                <p className="text-xs text-muted-foreground">{currentList.date}</p>
              </div>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" className="w-64">
            {shoppingLists.map((list) => (
              <DropdownMenuItem
                key={list.id}
                onClick={() => onListChange(list.id)}
                className={`${activeList === list.id ? 'bg-accent' : ''}`}
              >
                <div className="flex flex-col">
                  <span className="font-medium">{list.name}</span>
                  <span className="text-xs text-muted-foreground">{list.date}</span>
                </div>
              </DropdownMenuItem>
            ))}
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* Summary Stats */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <div className="text-center">
            <p className="text-lg font-semibold text-card-foreground">{totalItems}</p>
            <p className="text-xs text-muted-foreground">Items</p>
          </div>
          <div className="h-8 w-px bg-border"></div>
          <div className="text-center">
            <div className="flex items-center gap-1">
              <DollarSign className="w-3 h-3 text-muted-foreground" />
              <p className="text-lg font-semibold text-card-foreground">
                {estimatedCost.toFixed(2)}
              </p>
            </div>
            <p className="text-xs text-muted-foreground">Estimated</p>
          </div>
        </div>
        
        <Button className="bg-green-500 hover:bg-green-600 text-white">
          Generate from Meal Plan
        </Button>
      </div>
    </div>
  );
}