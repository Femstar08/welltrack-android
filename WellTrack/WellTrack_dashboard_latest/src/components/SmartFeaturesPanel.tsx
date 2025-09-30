import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from "./ui/collapsible";
import { 
  Lightbulb, 
  Package, 
  DollarSign, 
  TrendingUp, 
  ShoppingBasket,
  ChevronDown, 
  ChevronUp,
  Sparkles,
  Clock,
  Star
} from "lucide-react";
import { useState } from "react";

interface SmartSuggestion {
  id: string;
  title: string;
  description: string;
  type: 'substitute' | 'bulk' | 'seasonal' | 'deal' | 'complement';
  savings?: number;
  reason: string;
}

export function SmartFeaturesPanel() {
  const [alreadyHaveOpen, setAlreadyHaveOpen] = useState(true);
  const [substitutesOpen, setSubstitutesOpen] = useState(true);
  const [dealsOpen, setDealsOpen] = useState(false);

  const alreadyHaveItems = [
    { id: '1', name: 'Olive Oil', reason: 'Added 3 days ago', confidence: 95 },
    { id: '2', name: 'Salt', reason: 'Rarely runs out', confidence: 85 },
    { id: '3', name: 'Black Pepper', reason: 'Half full container', confidence: 75 }
  ];

  const substitutes: SmartSuggestion[] = [
    {
      id: '1',
      title: 'Greek Yogurt → Regular Yogurt',
      description: 'Regular yogurt available, save $2.00',
      type: 'substitute',
      savings: 2.00,
      reason: 'Similar nutritional profile'
    },
    {
      id: '2', 
      title: 'Bell Peppers → Frozen Mix',
      description: 'Frozen pepper mix, save $1.50',
      type: 'substitute',
      savings: 1.50,
      reason: 'For cooking applications'
    }
  ];

  const bulkDeals: SmartSuggestion[] = [
    {
      id: '3',
      title: 'Buy 2 Avocados, Get 1 Free',
      description: 'Limited time offer, save $0.75',
      type: 'bulk',
      savings: 0.75,
      reason: 'Store promotion'
    },
    {
      id: '4',
      title: 'Quinoa Family Size',
      description: '2lb bag vs 1lb, save $3.00',
      type: 'bulk',
      savings: 3.00,
      reason: '40% better value'
    }
  ];

  const seasonalSuggestions: SmartSuggestion[] = [
    {
      id: '5',
      title: 'Winter Squash',
      description: 'Peak season, great prices',
      type: 'seasonal',
      reason: 'High nutrition, versatile'
    },
    {
      id: '6',
      title: 'Citrus Fruits',
      description: 'Oranges & grapefruits in season',
      type: 'seasonal',
      reason: 'Boost vitamin C intake'
    }
  ];

  const SuggestionCard = ({ suggestion }: { suggestion: SmartSuggestion }) => (
    <Card className="bg-card hover:bg-accent/50 transition-colors cursor-pointer border">
      <CardContent className="p-3">
        <div className="space-y-2">
          <div className="flex items-start justify-between">
            <h4 className="text-sm font-medium text-card-foreground">{suggestion.title}</h4>
            {suggestion.savings && (
              <Badge className="bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200 text-xs">
                Save ${suggestion.savings.toFixed(2)}
              </Badge>
            )}
          </div>
          
          <p className="text-xs text-muted-foreground">{suggestion.description}</p>
          <p className="text-xs text-blue-600">{suggestion.reason}</p>
          
          <Button size="sm" variant="outline" className="w-full h-7 text-xs">
            Apply Suggestion
          </Button>
        </div>
      </CardContent>
    </Card>
  );

  return (
    <div className="bg-card h-full overflow-y-auto">
      <div className="p-4 space-y-4">
        {/* Header */}
        <div className="flex items-center gap-2">
          <Lightbulb className="w-5 h-5 text-orange-500" />
          <h3 className="font-medium text-card-foreground">Smart Shopping</h3>
        </div>

        {/* Already Have Suggestions */}
        <Collapsible open={alreadyHaveOpen} onOpenChange={setAlreadyHaveOpen}>
          <CollapsibleTrigger className="w-full">
            <Card className="bg-green-50 border-green-200 dark:bg-green-950 dark:border-green-800">
              <CardHeader className="pb-2 px-3 py-2">
                <CardTitle className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Package className="w-4 h-4 text-green-600" />
                    <span className="text-sm text-green-800 dark:text-green-200">Already Have</span>
                  </div>
                  {alreadyHaveOpen ? (
                    <ChevronUp className="w-4 h-4 text-green-600" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-green-600" />
                  )}
                </CardTitle>
              </CardHeader>
            </Card>
          </CollapsibleTrigger>
          <CollapsibleContent className="space-y-2 mt-2">
            {alreadyHaveItems.map((item) => (
              <Card key={item.id} className="bg-card border">
                <CardContent className="p-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <h4 className="text-sm font-medium text-card-foreground">{item.name}</h4>
                      <p className="text-xs text-muted-foreground">{item.reason}</p>
                    </div>
                    <div className="flex items-center gap-2">
                      <span className="text-xs text-green-600">{item.confidence}% sure</span>
                      <Button size="sm" variant="outline" className="h-6 text-xs px-2">
                        Remove
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </CollapsibleContent>
        </Collapsible>

        {/* Substitutes */}
        <Collapsible open={substitutesOpen} onOpenChange={setSubstitutesOpen}>
          <CollapsibleTrigger className="w-full">
            <Card className="bg-blue-50 border-blue-200 dark:bg-blue-950 dark:border-blue-800">
              <CardHeader className="pb-2 px-3 py-2">
                <CardTitle className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Sparkles className="w-4 h-4 text-blue-600" />
                    <span className="text-sm text-blue-800 dark:text-blue-200">Smart Substitutes</span>
                  </div>
                  {substitutesOpen ? (
                    <ChevronUp className="w-4 h-4 text-blue-600" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-blue-600" />
                  )}
                </CardTitle>
              </CardHeader>
            </Card>
          </CollapsibleTrigger>
          <CollapsibleContent className="space-y-2 mt-2">
            {substitutes.map((suggestion) => (
              <SuggestionCard key={suggestion.id} suggestion={suggestion} />
            ))}
          </CollapsibleContent>
        </Collapsible>

        {/* Bulk Deals & Promotions */}
        <Collapsible open={dealsOpen} onOpenChange={setDealsOpen}>
          <CollapsibleTrigger className="w-full">
            <Card className="bg-purple-50 border-purple-200 dark:bg-purple-950 dark:border-purple-800">
              <CardHeader className="pb-2 px-3 py-2">
                <CardTitle className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <DollarSign className="w-4 h-4 text-purple-600" />
                    <span className="text-sm text-purple-800 dark:text-purple-200">Deals & Bulk</span>
                  </div>
                  {dealsOpen ? (
                    <ChevronUp className="w-4 h-4 text-purple-600" />
                  ) : (
                    <ChevronDown className="w-4 h-4 text-purple-600" />
                  )}
                </CardTitle>
              </CardHeader>
            </Card>
          </CollapsibleTrigger>
          <CollapsibleContent className="space-y-2 mt-2">
            {bulkDeals.map((deal) => (
              <SuggestionCard key={deal.id} suggestion={deal} />
            ))}
          </CollapsibleContent>
        </Collapsible>

        {/* Seasonal Recommendations */}
        <Card className="bg-orange-50 border-orange-200 dark:bg-orange-950 dark:border-orange-800">
          <CardHeader className="pb-2 px-3 py-2">
            <CardTitle className="flex items-center gap-2">
              <Clock className="w-4 h-4 text-orange-600" />
              <span className="text-sm text-orange-800 dark:text-orange-200">Seasonal Picks</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="px-3 pb-3 pt-0 space-y-2">
            {seasonalSuggestions.map((suggestion) => (
              <SuggestionCard key={suggestion.id} suggestion={suggestion} />
            ))}
          </CardContent>
        </Card>

        {/* Don't Forget */}
        <Card className="bg-yellow-50 border-yellow-200 dark:bg-yellow-950 dark:border-yellow-800">
          <CardHeader className="pb-2 px-3 py-2">
            <CardTitle className="flex items-center gap-2">
              <Star className="w-4 h-4 text-yellow-600" />
              <span className="text-sm text-yellow-800 dark:text-yellow-200">Don't Forget</span>
            </CardTitle>
          </CardHeader>
          <CardContent className="px-3 pb-3 pt-0 space-y-2">
            <div className="text-xs text-muted-foreground space-y-1">
              <p>• Reusable bags (left in car)</p>
              <p>• Check expiry dates on dairy</p>
              <p>• Look for organic produce deals</p>
              <p>• Pharmacy pickup available</p>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}