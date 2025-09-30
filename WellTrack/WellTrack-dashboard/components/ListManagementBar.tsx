import { Button } from "./ui/button";
import { Switch } from "./ui/switch";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "./ui/dropdown-menu";
import { Sparkles, Package, Trash2, Settings, Lightbulb } from "lucide-react";

interface ListManagementBarProps {
  onAutoGenerate: () => void;
  autoAddPantry: boolean;
  onAutoAddPantryChange: (enabled: boolean) => void;
  onShowSmartFeatures: () => void;
  showSmartFeatures: boolean;
}

export function ListManagementBar({
  onAutoGenerate,
  autoAddPantry,
  onAutoAddPantryChange,
  onShowSmartFeatures,
  showSmartFeatures
}: ListManagementBarProps) {
  return (
    <div className="bg-card border-b border-border px-4 py-3">
      <div className="flex items-center justify-between gap-3">
        {/* Primary Actions */}
        <div className="flex items-center gap-2">
          <Button 
            onClick={onAutoGenerate}
            className="bg-blue-500 hover:bg-blue-600 text-white gap-2"
            size="sm"
          >
            <Sparkles className="w-4 h-4" />
            Auto-Generate
          </Button>

          {/* Pantry Low Stock Toggle */}
          <div className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-accent/50">
            <Package className="w-4 h-4 text-muted-foreground" />
            <span className="text-sm text-muted-foreground">Pantry</span>
            <Switch 
              checked={autoAddPantry}
              onCheckedChange={onAutoAddPantryChange}
              className="scale-75"
            />
          </div>
        </div>

        {/* Secondary Actions */}
        <div className="flex items-center gap-2">
          {/* Clear Completed */}
          <Button variant="outline" size="sm" className="gap-1.5">
            <Trash2 className="w-4 h-4" />
            Clear Done
          </Button>

          {/* Smart Features Toggle */}
          <Button 
            variant={showSmartFeatures ? "default" : "outline"} 
            size="sm" 
            onClick={onShowSmartFeatures}
            className="gap-1.5"
          >
            <Lightbulb className="w-4 h-4" />
            Smart
          </Button>

          {/* Settings */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm" className="gap-1.5">
                <Settings className="w-4 h-4" />
                Settings
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem>Store Layout</DropdownMenuItem>
              <DropdownMenuItem>Categories</DropdownMenuItem>
              <DropdownMenuItem>Price Settings</DropdownMenuItem>
              <DropdownMenuItem>Notification Settings</DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
    </div>
  );
}