import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Progress } from "./ui/progress";
import { 
  Brain,
  Lightbulb,
  TrendingUp,
  AlertTriangle,
  Clock,
  Utensils,
  Zap,
  Target,
  DollarSign,
  Package,
  Star,
  Calendar
} from "lucide-react";
import { Supplement } from "./Supplements";

interface SmartRecommendationsProps {
  supplements: Supplement[];
  adherencePercentage: number;
}

interface Recommendation {
  id: string;
  type: 'timing' | 'dosage' | 'interaction' | 'nutrition' | 'cost' | 'inventory';
  priority: 'high' | 'medium' | 'low';
  title: string;
  description: string;
  impact: string;
  confidence: number;
  actionable: boolean;
  supplement?: string;
}

export function SmartRecommendations({ supplements, adherencePercentage }: SmartRecommendationsProps) {
  // Generate smart recommendations based on data analysis
  const recommendations: Recommendation[] = [
    {
      id: '1',
      type: 'timing',
      priority: 'high',
      title: 'Optimize Magnesium Timing',
      description: 'Your magnesium is currently scheduled for bedtime, but taking it 2 hours before bed may improve sleep quality and absorption.',
      impact: 'Better sleep quality and 15% improved absorption',
      confidence: 87,
      actionable: true,
      supplement: 'Magnesium Glycinate'
    },
    {
      id: '2',
      type: 'interaction',
      priority: 'high',
      title: 'Calcium-Iron Separation',
      description: 'Taking calcium and iron together reduces iron absorption by up to 60%. Space them at least 2 hours apart.',
      impact: 'Maximize iron absorption and prevent deficiency',
      confidence: 95,
      actionable: true
    },
    {
      id: '3',
      type: 'nutrition',
      priority: 'medium',
      title: 'Consider Vitamin K2',
      description: 'Since you take Vitamin D3 and have calcium in your diet, adding K2 would optimize calcium utilization.',
      impact: 'Enhanced bone health and cardiovascular protection',
      confidence: 78,
      actionable: true
    },
    {
      id: '4',
      type: 'dosage',
      priority: 'medium',
      title: 'Split Omega-3 Dose',
      description: 'Your current 2000mg omega-3 dose would be better absorbed as two 1000mg doses with meals.',
      impact: '25% better absorption and reduced GI discomfort',
      confidence: 82,
      actionable: true,
      supplement: 'Omega-3 Fish Oil'
    },
    {
      id: '5',
      type: 'inventory',
      priority: 'high',
      title: 'Reorder Alert',
      description: 'Your Whey Protein will run out in 8 days based on current usage. Order now to avoid gaps.',
      impact: 'Prevent supplementation gaps',
      confidence: 100,
      actionable: true,
      supplement: 'Whey Protein'
    },
    {
      id: '6',
      type: 'cost',
      priority: 'low',
      title: 'Bundle Savings Opportunity',
      description: 'Switching to the same brand for Vitamin D3 and B-Complex could save $15/month with bundle pricing.',
      impact: 'Save $180 annually while maintaining quality',
      confidence: 90,
      actionable: true
    }
  ];

  // Nutritional gap analysis
  const nutritionalGaps = [
    {
      nutrient: 'Vitamin K2',
      currentIntake: 0,
      recommendedIntake: 100,
      unit: 'mcg',
      sources: ['MK-7 supplement', 'Fermented foods', 'Grass-fed dairy'],
      priority: 'high',
      reason: 'Supports calcium metabolism with your Vitamin D3'
    },
    {
      nutrient: 'Zinc',
      currentIntake: 8,
      recommendedIntake: 15,
      unit: 'mg',
      sources: ['Zinc bisglycinate', 'Oysters', 'Beef'],
      priority: 'medium',
      reason: 'Immune support and protein synthesis'
    },
    {
      nutrient: 'Curcumin',
      currentIntake: 0,
      recommendedIntake: 500,
      unit: 'mg',
      sources: ['Curcumin with piperine', 'Turmeric root', 'Golden milk'],
      priority: 'low',
      reason: 'Anti-inflammatory support for active lifestyle'
    }
  ];

  // Seasonal recommendations
  const seasonalRecommendations = [
    {
      season: 'Winter',
      recommendations: ['Increase Vitamin D3 to 4000 IU', 'Add Vitamin C for immune support', 'Consider probiotics'],
      rationale: 'Reduced sun exposure and cold/flu season'
    },
    {
      season: 'Spring',
      recommendations: ['Add quercetin for allergies', 'Maintain current regimen', 'Consider liver support'],
      rationale: 'Allergy season and natural detox time'
    }
  ];

  const getRecommendationIcon = (type: string) => {
    switch (type) {
      case 'timing': return <Clock className="w-4 h-4 text-blue-500" />;
      case 'dosage': return <Target className="w-4 h-4 text-green-500" />;
      case 'interaction': return <AlertTriangle className="w-4 h-4 text-red-500" />;
      case 'nutrition': return <Utensils className="w-4 h-4 text-purple-500" />;
      case 'cost': return <DollarSign className="w-4 h-4 text-yellow-500" />;
      case 'inventory': return <Package className="w-4 h-4 text-orange-500" />;
      default: return <Lightbulb className="w-4 h-4 text-blue-500" />;
    }
  };

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high': return 'bg-red-100 text-red-800 dark:bg-red-950 dark:text-red-200';
      case 'medium': return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-950 dark:text-yellow-200';
      case 'low': return 'bg-green-100 text-green-800 dark:bg-green-950 dark:text-green-200';
      default: return 'bg-muted text-muted-foreground';
    }
  };

  const getGapPriorityColor = (priority: string) => {
    switch (priority) {
      case 'high': return 'text-red-600 bg-red-100 dark:bg-red-950';
      case 'medium': return 'text-yellow-600 bg-yellow-100 dark:bg-yellow-950';
      case 'low': return 'text-green-600 bg-green-100 dark:bg-green-950';
      default: return 'text-muted-foreground bg-muted';
    }
  };

  const highPriorityRecommendations = recommendations.filter(r => r.priority === 'high');
  const mediumPriorityRecommendations = recommendations.filter(r => r.priority === 'medium');
  const lowPriorityRecommendations = recommendations.filter(r => r.priority === 'low');

  return (
    <div className="h-full overflow-y-auto p-4 space-y-6">
      {/* AI Insights Header */}
      <Card className="bg-purple-50 dark:bg-purple-950 border-purple-200 dark:border-purple-800">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-purple-800 dark:text-purple-200">
            <Brain className="w-5 h-5" />
            Smart Supplement Insights
            <Badge className="bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200 ml-auto">
              {recommendations.length} recommendations
            </Badge>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-purple-700 dark:text-purple-300 mb-3">
            Based on your supplement regimen, adherence patterns, and nutritional data, here are personalized optimization recommendations.
          </p>
          
          <div className="grid grid-cols-3 gap-4 text-center">
            <div>
              <p className="text-lg font-semibold text-purple-800 dark:text-purple-200">
                {adherencePercentage}%
              </p>
              <p className="text-xs text-purple-700 dark:text-purple-300">Adherence Rate</p>
            </div>
            <div>
              <p className="text-lg font-semibold text-purple-800 dark:text-purple-200">
                {highPriorityRecommendations.length}
              </p>
              <p className="text-xs text-purple-700 dark:text-purple-300">High Priority</p>
            </div>
            <div>
              <p className="text-lg font-semibold text-purple-800 dark:text-purple-200">
                {recommendations.filter(r => r.actionable).length}
              </p>
              <p className="text-xs text-purple-700 dark:text-purple-300">Actionable Items</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* High Priority Recommendations */}
      {highPriorityRecommendations.length > 0 && (
        <div className="space-y-3">
          <h3 className="font-medium text-card-foreground flex items-center gap-2">
            <AlertTriangle className="w-4 h-4 text-red-500" />
            High Priority Actions
          </h3>
          
          {highPriorityRecommendations.map((rec) => (
            <Card key={rec.id} className="border-red-200 dark:border-red-800">
              <CardContent className="p-4">
                <div className="space-y-3">
                  <div className="flex items-start justify-between">
                    <div className="flex items-start gap-3 flex-1">
                      {getRecommendationIcon(rec.type)}
                      <div>
                        <h4 className="font-medium text-card-foreground">{rec.title}</h4>
                        {rec.supplement && (
                          <p className="text-xs text-muted-foreground">
                            Affects: {rec.supplement}
                          </p>
                        )}
                        <p className="text-sm text-muted-foreground mt-1">{rec.description}</p>
                      </div>
                    </div>
                    <div className="flex flex-col items-end gap-1">
                      <Badge className={`text-xs px-2 py-1 ${getPriorityColor(rec.priority)}`}>
                        {rec.priority}
                      </Badge>
                      <span className="text-xs text-muted-foreground">{rec.confidence}% confident</span>
                    </div>
                  </div>

                  <div className="bg-green-50 dark:bg-green-950 border border-green-200 dark:border-green-800 rounded-lg p-3">
                    <div className="flex items-center gap-2 mb-1">
                      <TrendingUp className="w-3 h-3 text-green-600" />
                      <span className="text-sm font-medium text-green-800 dark:text-green-200">Expected Impact</span>
                    </div>
                    <p className="text-sm text-green-700 dark:text-green-300">{rec.impact}</p>
                  </div>

                  {rec.actionable && (
                    <div className="flex gap-2">
                      <Button size="sm" className="flex-1">
                        Apply Recommendation
                      </Button>
                      <Button variant="outline" size="sm">
                        Learn More
                      </Button>
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Nutritional Gap Analysis */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Target className="w-4 h-4 text-green-500" />
            Nutritional Gap Analysis
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground mb-4">
            Based on your current supplement regimen and dietary patterns, these nutrients may need attention.
          </p>

          {nutritionalGaps.map((gap, index) => (
            <div key={index} className="p-4 bg-muted/30 rounded-lg">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h4 className="font-medium text-card-foreground">{gap.nutrient}</h4>
                  <p className="text-sm text-muted-foreground">{gap.reason}</p>
                </div>
                <Badge className={`text-xs px-2 py-1 ${getGapPriorityColor(gap.priority)}`}>
                  {gap.priority} priority
                </Badge>
              </div>

              <div className="space-y-3">
                <div className="space-y-1">
                  <div className="flex justify-between text-sm">
                    <span className="text-muted-foreground">Current vs Recommended</span>
                    <span className="font-medium">
                      {gap.currentIntake} / {gap.recommendedIntake} {gap.unit}
                    </span>
                  </div>
                  <Progress 
                    value={(gap.currentIntake / gap.recommendedIntake) * 100} 
                    className="h-2" 
                  />
                </div>

                <div>
                  <p className="text-sm font-medium text-card-foreground mb-2">Best Sources:</p>
                  <div className="flex flex-wrap gap-1">
                    {gap.sources.map((source, idx) => (
                      <Badge key={idx} variant="secondary" className="text-xs">
                        {source}
                      </Badge>
                    ))}
                  </div>
                </div>
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Medium Priority Recommendations */}
      {mediumPriorityRecommendations.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Zap className="w-4 h-4 text-yellow-500" />
              Optimization Opportunities
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {mediumPriorityRecommendations.map((rec) => (
              <div key={rec.id} className="p-3 bg-muted/30 rounded-lg">
                <div className="flex items-start justify-between">
                  <div className="flex items-start gap-3 flex-1">
                    {getRecommendationIcon(rec.type)}
                    <div>
                      <h4 className="font-medium text-card-foreground">{rec.title}</h4>
                      <p className="text-sm text-muted-foreground mt-1">{rec.description}</p>
                      <p className="text-xs text-green-600 mt-2">{rec.impact}</p>
                    </div>
                  </div>
                  <Badge className={`text-xs px-2 py-1 ${getPriorityColor(rec.priority)}`}>
                    {rec.priority}
                  </Badge>
                </div>
                
                {rec.actionable && (
                  <Button variant="outline" size="sm" className="w-full mt-3">
                    Consider This Change
                  </Button>
                )}
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {/* Seasonal Recommendations */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Calendar className="w-4 h-4 text-blue-500" />
            Seasonal Adjustments
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {seasonalRecommendations.map((seasonal, index) => (
            <div key={index} className="p-3 bg-blue-50 dark:bg-blue-950 rounded-lg border border-blue-200 dark:border-blue-800">
              <h4 className="font-medium text-blue-800 dark:text-blue-200 mb-2">
                {seasonal.season} Recommendations
              </h4>
              <p className="text-sm text-blue-700 dark:text-blue-300 mb-3">
                {seasonal.rationale}
              </p>
              <div className="space-y-1">
                {seasonal.recommendations.map((rec, idx) => (
                  <div key={idx} className="flex items-center gap-2 text-sm">
                    <div className="w-1.5 h-1.5 bg-blue-500 rounded-full"></div>
                    <span className="text-blue-800 dark:text-blue-200">{rec}</span>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </CardContent>
      </Card>

      {/* Cost Optimization */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <DollarSign className="w-4 h-4 text-green-500" />
            Cost Optimization Insights
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 gap-4 mb-4">
            <div className="text-center">
              <p className="text-xl font-semibold text-card-foreground">
                ${supplements.reduce((sum, s) => sum + s.cost, 0).toFixed(2)}
              </p>
              <p className="text-sm text-muted-foreground">Daily Cost</p>
            </div>
            <div className="text-center">
              <p className="text-xl font-semibold text-green-600">
                $12.50
              </p>
              <p className="text-sm text-muted-foreground">Potential Savings</p>
            </div>
          </div>
          
          <div className="space-y-2">
            <div className="flex justify-between text-sm">
              <span className="text-muted-foreground">Cost per serving efficiency</span>
              <span className="font-medium text-card-foreground">87%</span>
            </div>
            <Progress value={87} className="h-2" />
          </div>
          
          <Button variant="outline" className="w-full mt-4">
            View Detailed Cost Analysis
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}