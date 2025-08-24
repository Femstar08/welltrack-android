import { Button } from "./ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { Plus, Scan, Camera, Pill, BookOpen, Zap, ChevronDown, ChevronUp } from "lucide-react";
import { useState } from "react";

export function QuickActions() {
  const [isOpen, setIsOpen] = useState(false);
  
  return (
    <div className="space-y-4">
      {/* Floating Action Button */}
      <div className="flex justify-center">
        <Button
          size="lg"
          className="bg-green-500 hover:bg-green-600 text-white rounded-full w-16 h-16 shadow-lg hover:shadow-xl transition-all duration-200"
        >
          <Plus className="w-8 h-8" />
        </Button>
      </div>
      
      {/* Quick Action Buttons */}
      <Card className="bg-card shadow-sm border border-border">
        <Collapsible open={isOpen} onOpenChange={setIsOpen}>
          <CollapsibleTrigger className="w-full">
            <CardHeader className="pb-3 px-4 py-4">
              <CardTitle className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <Zap className="w-6 h-6 text-orange-600" />
                  <span className="text-lg text-card-foreground">Quick Actions</span>
                </div>
                {isOpen ? (
                  <ChevronUp className="w-5 h-5 text-muted-foreground" />
                ) : (
                  <ChevronDown className="w-5 h-5 text-muted-foreground" />
                )}
              </CardTitle>
            </CardHeader>
          </CollapsibleTrigger>
          
          <CollapsibleContent>
            <CardContent className="px-4 pb-4">
              <div className="grid grid-cols-2 gap-4">
                <Button variant="outline" className="flex flex-col items-center space-y-3 h-24 border-green-200 hover:bg-green-50 text-base dark:border-green-800 dark:hover:bg-green-950">
                  <Scan className="w-7 h-7 text-green-600" />
                  <span>Scan Food</span>
                </Button>
                <Button variant="outline" className="flex flex-col items-center space-y-3 h-24 border-blue-200 hover:bg-blue-50 text-base dark:border-blue-800 dark:hover:bg-blue-950">
                  <Camera className="w-7 h-7 text-blue-600" />
                  <span>Photo Log</span>
                </Button>
                <Button variant="outline" className="flex flex-col items-center space-y-3 h-24 border-purple-200 hover:bg-purple-50 text-base dark:border-purple-800 dark:hover:bg-purple-950">
                  <Pill className="w-7 h-7 text-purple-600" />
                  <span>Log Supplement</span>
                </Button>
                <Button variant="outline" className="flex flex-col items-center space-y-3 h-24 border-orange-200 hover:bg-orange-50 text-base dark:border-orange-800 dark:hover:bg-orange-950">
                  <BookOpen className="w-7 h-7 text-orange-600" />
                  <span>Add Recipe</span>
                </Button>
              </div>
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>
    </div>
  );
}