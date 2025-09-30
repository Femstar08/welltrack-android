import { Button } from "./ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { Plus, Scan, Camera, Pill, BookOpen, Zap, ChevronDown, ChevronUp, BarChart3, Activity } from "lucide-react";
import { useState } from "react";

interface QuickActionsProps {
  onNavigate?: (screen: string) => void;
}

export function QuickActions({ onNavigate }: QuickActionsProps) {
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
                <Button 
                  variant="outline" 
                  className="flex flex-col items-center space-y-3 h-24 border-green-200 hover:bg-green-50 text-base dark:border-green-800 dark:hover:bg-green-950"
                  onClick={() => console.log('Scan Food')}
                >
                  <Scan className="w-7 h-7 text-green-600" />
                  <span>Scan Food</span>
                </Button>
                <Button 
                  variant="outline" 
                  className="flex flex-col items-center space-y-3 h-24 border-blue-200 hover:bg-blue-50 text-base dark:border-blue-800 dark:hover:bg-blue-950"
                  onClick={() => console.log('Photo Log')}
                >
                  <Camera className="w-7 h-7 text-blue-600" />
                  <span>Photo Log</span>
                </Button>
              </div>
              
              {/* App Access Section */}
              <div className="pt-4 border-t border-border">
                <h3 className="text-sm font-medium text-muted-foreground mb-3">All Apps</h3>
                <div className="grid grid-cols-2 gap-3">
                  <Button 
                    variant="outline" 
                    className="flex items-center gap-2 h-12 justify-start px-3 border-purple-200 hover:bg-purple-50 text-sm dark:border-purple-800 dark:hover:bg-purple-950"
                    onClick={() => onNavigate?.('supplements')}
                  >
                    <Pill className="w-5 h-5 text-purple-600" />
                    <span>Supplements</span>
                  </Button>
                  <Button 
                    variant="outline" 
                    className="flex items-center gap-2 h-12 justify-start px-3 border-orange-200 hover:bg-orange-50 text-sm dark:border-orange-800 dark:hover:bg-orange-950"
                    onClick={() => onNavigate?.('recipes')}
                  >
                    <BookOpen className="w-5 h-5 text-orange-600" />
                    <span>Recipes</span>
                  </Button>
                  <Button 
                    variant="outline" 
                    className="flex items-center gap-2 h-12 justify-start px-3 border-yellow-200 hover:bg-yellow-50 text-sm dark:border-yellow-800 dark:hover:bg-yellow-950"
                    onClick={() => onNavigate?.('health-analytics')}
                  >
                    <BarChart3 className="w-5 h-5 text-yellow-600" />
                    <span>Analytics</span>
                  </Button>
                  <Button 
                    variant="outline" 
                    className="flex items-center gap-2 h-12 justify-start px-3 border-red-200 hover:bg-red-50 text-sm dark:border-red-800 dark:hover:bg-red-950"
                    onClick={() => onNavigate?.('biomarkers')}
                  >
                    <Activity className="w-5 h-5 text-red-600" />
                    <span>Biomarkers</span>
                  </Button>
                </div>
              </div>
            </CardContent>
          </CollapsibleContent>
        </Collapsible>
      </Card>
    </div>
  );
}