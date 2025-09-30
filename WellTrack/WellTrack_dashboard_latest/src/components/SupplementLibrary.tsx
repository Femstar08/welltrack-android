import { useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Progress } from "./ui/progress";
import { Input } from "./ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "./ui/select";
import { 
  Search,
  Filter,
  Plus,
  Edit,
  Trash2,
  Star,
  Calendar,
  DollarSign,
  Package,
  AlertTriangle,
  Pill,
  Coffee,
  Utensils,
  Moon,
  Dumbbell
} from "lucide-react";
import { Supplement, SupplementSchedule } from "./Supplements";

interface SupplementLibraryProps {
  supplements: Supplement[];
  schedules: SupplementSchedule[];
}

export function SupplementLibrary({ supplements, schedules }: SupplementLibraryProps) {
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState<string>("all");
  const [sortBy, setSortBy] = useState<string>("name");

  const categories = [
    { value: "all", label: "All Supplements" },
    { value: "vitamin", label: "Vitamins" },
    { value: "mineral", label: "Minerals" },
    { value: "omega", label: "Omega-3/Fats" },
    { value: "amino", label: "Amino Acids" },
    { value: "herb", label: "Herbs" },
    { value: "probiotic", label: "Probiotics" },
    { value: "other", label: "Other" }
  ];

  // Filter and sort supplements
  const filteredSupplements = supplements
    .filter(supplement => {
      const matchesSearch = supplement.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                           supplement.brand.toLowerCase().includes(searchQuery.toLowerCase()) ||
                           supplement.purpose.toLowerCase().includes(searchQuery.toLowerCase());
      const matchesCategory = selectedCategory === "all" || supplement.category === selectedCategory;
      return matchesSearch && matchesCategory;
    })
    .sort((a, b) => {
      switch (sortBy) {
        case "name":
          return a.name.localeCompare(b.name);
        case "effectiveness":
          return b.effectiveness - a.effectiveness;
        case "stock":
          return (a.stockLevel / a.totalStock) - (b.stockLevel / b.totalStock);
        case "expiry":
          return new Date(a.expiryDate).getTime() - new Date(b.expiryDate).getTime();
        default:
          return 0;
      }
    });

  const getStockLevel = (current: number, total: number) => {
    const percentage = (current / total) * 100;
    if (percentage <= 25) return { level: 'Critical', color: 'text-red-600 bg-red-100 dark:bg-red-950', bgColor: 'bg-red-500' };
    if (percentage <= 50) return { level: 'Low', color: 'text-yellow-600 bg-yellow-100 dark:bg-yellow-950', bgColor: 'bg-yellow-500' };
    if (percentage <= 75) return { level: 'Medium', color: 'text-blue-600 bg-blue-100 dark:bg-blue-950', bgColor: 'bg-blue-500' };
    return { level: 'Good', color: 'text-green-600 bg-green-100 dark:bg-green-950', bgColor: 'bg-green-500' };
  };

  const getExpiryStatus = (expiryDate: string) => {
    const expiry = new Date(expiryDate);
    const now = new Date();
    const daysDiff = Math.ceil((expiry.getTime() - now.getTime()) / (1000 * 60 * 60 * 24));
    
    if (daysDiff <= 0) return { status: 'Expired', color: 'text-red-600 bg-red-100 dark:bg-red-950' };
    if (daysDiff <= 30) return { status: 'Expires Soon', color: 'text-yellow-600 bg-yellow-100 dark:bg-yellow-950' };
    if (daysDiff <= 90) return { status: 'Expiring', color: 'text-blue-600 bg-blue-100 dark:bg-blue-950' };
    return { status: 'Fresh', color: 'text-green-600 bg-green-100 dark:bg-green-950' };
  };

  const getScheduleInfo = (supplementId: string) => {
    const schedule = schedules.find(s => s.supplementId === supplementId && s.active);
    if (!schedule) return null;

    const timeSlots = [];
    if (schedule.timeSlots.morning) timeSlots.push('Morning');
    if (schedule.timeSlots.preWorkout) timeSlots.push('Pre-workout');
    if (schedule.timeSlots.afternoon) timeSlots.push('Afternoon');
    if (schedule.timeSlots.evening) timeSlots.push('Evening');
    if (schedule.timeSlots.bedtime) timeSlots.push('Bedtime');

    return {
      frequency: schedule.frequency,
      timeSlots,
      withFood: schedule.withFood
    };
  };

  const renderStars = (rating: number) => {
    return (
      <div className="flex items-center gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star 
            key={star}
            className={`w-3 h-3 ${star <= rating ? 'text-yellow-500 fill-current' : 'text-muted-foreground'}`}
          />
        ))}
        <span className="text-xs text-muted-foreground ml-1">({rating})</span>
      </div>
    );
  };

  return (
    <div className="h-full overflow-y-auto p-4 space-y-4">
      {/* Search and Filter Bar */}
      <div className="space-y-3">
        <div className="flex gap-2">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
            <Input
              placeholder="Search supplements..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-10"
            />
          </div>
          <Button size="sm" className="gap-2">
            <Plus className="w-4 h-4" />
            Add New
          </Button>
        </div>

        <div className="flex gap-2">
          <Select value={selectedCategory} onValueChange={setSelectedCategory}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Category" />
            </SelectTrigger>
            <SelectContent>
              {categories.map((category) => (
                <SelectItem key={category.value} value={category.value}>
                  {category.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>

          <Select value={sortBy} onValueChange={setSortBy}>
            <SelectTrigger className="w-36">
              <SelectValue placeholder="Sort by" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="name">Name A-Z</SelectItem>
              <SelectItem value="effectiveness">Effectiveness</SelectItem>
              <SelectItem value="stock">Stock Level</SelectItem>
              <SelectItem value="expiry">Expiry Date</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Supplements Grid */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="font-medium text-card-foreground">
            Your Supplements ({filteredSupplements.length})
          </h3>
        </div>

        {filteredSupplements.map((supplement) => {
          const stockInfo = getStockLevel(supplement.stockLevel, supplement.totalStock);
          const expiryInfo = getExpiryStatus(supplement.expiryDate);
          const scheduleInfo = getScheduleInfo(supplement.id);
          const stockPercentage = (supplement.stockLevel / supplement.totalStock) * 100;

          return (
            <Card key={supplement.id} className="hover:bg-accent/50 transition-colors">
              <CardContent className="p-4">
                <div className="space-y-4">
                  {/* Header */}
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <h4 className="font-medium text-card-foreground">
                          {supplement.name}
                        </h4>
                        <Badge className={`text-xs px-2 py-1 ${stockInfo.color}`}>
                          {stockInfo.level}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {supplement.brand} • {supplement.dosage}{supplement.unit} {supplement.form}
                      </p>
                      <p className="text-sm text-muted-foreground mt-1">
                        {supplement.purpose}
                      </p>
                    </div>
                    
                    <div className="flex items-center gap-1">
                      <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                        <Edit className="w-4 h-4" />
                      </Button>
                      <Button variant="ghost" size="sm" className="h-8 w-8 p-0 text-red-500 hover:text-red-700">
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>

                  {/* Stats Row */}
                  <div className="grid grid-cols-4 gap-3 text-center">
                    <div>
                      <div className="flex items-center justify-center gap-1 mb-1">
                        <Package className="w-3 h-3 text-muted-foreground" />
                        <span className="text-sm font-medium">{supplement.stockLevel}</span>
                      </div>
                      <p className="text-xs text-muted-foreground">Stock</p>
                    </div>
                    <div>
                      <div className="flex items-center justify-center gap-1 mb-1">
                        <Calendar className="w-3 h-3 text-muted-foreground" />
                        <span className="text-sm font-medium">
                          {Math.ceil((new Date(supplement.expiryDate).getTime() - new Date().getTime()) / (1000 * 60 * 60 * 24))}d
                        </span>
                      </div>
                      <p className="text-xs text-muted-foreground">Until expiry</p>
                    </div>
                    <div>
                      <div className="flex items-center justify-center gap-1 mb-1">
                        <DollarSign className="w-3 h-3 text-muted-foreground" />
                        <span className="text-sm font-medium">${supplement.cost.toFixed(2)}</span>
                      </div>
                      <p className="text-xs text-muted-foreground">Per serving</p>
                    </div>
                    <div>
                      <div className="flex justify-center mb-1">
                        {renderStars(supplement.effectiveness)}
                      </div>
                      <p className="text-xs text-muted-foreground">Effectiveness</p>
                    </div>
                  </div>

                  {/* Stock Progress */}
                  <div className="space-y-2">
                    <div className="flex justify-between text-sm">
                      <span className="text-muted-foreground">Stock Level</span>
                      <span className="font-medium text-card-foreground">
                        {supplement.stockLevel}/{supplement.totalStock}
                      </span>
                    </div>
                    <Progress value={stockPercentage} className="h-2" />
                  </div>

                  {/* Schedule Info */}
                  {scheduleInfo && (
                    <div className="bg-muted/30 rounded-lg p-3">
                      <div className="flex items-center gap-2 mb-2">
                        <Pill className="w-4 h-4 text-purple-500" />
                        <span className="text-sm font-medium text-card-foreground">Schedule</span>
                      </div>
                      <div className="space-y-1">
                        <p className="text-xs text-muted-foreground">
                          {scheduleInfo.timeSlots.join(', ')} • {scheduleInfo.frequency}
                        </p>
                        {scheduleInfo.withFood && (
                          <div className="flex items-center gap-1">
                            <Utensils className="w-3 h-3 text-muted-foreground" />
                            <span className="text-xs text-muted-foreground">Take with food</span>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  {/* Expiry Warning */}
                  {expiryInfo.status !== 'Fresh' && (
                    <div className={`flex items-center gap-2 p-2 rounded-lg ${expiryInfo.color}`}>
                      <AlertTriangle className="w-4 h-4" />
                      <span className="text-sm font-medium">
                        {expiryInfo.status}: {supplement.expiryDate}
                      </span>
                    </div>
                  )}

                  {/* Side Effects & Interactions */}
                  {(supplement.sideEffects || supplement.interactions) && (
                    <div className="space-y-2">
                      {supplement.sideEffects && (
                        <div className="bg-yellow-50 dark:bg-yellow-950 border border-yellow-200 dark:border-yellow-800 rounded-lg p-2">
                          <p className="text-xs font-medium text-yellow-800 dark:text-yellow-200">
                            Side Effects: {supplement.sideEffects}
                          </p>
                        </div>
                      )}
                      {supplement.interactions && supplement.interactions.length > 0 && (
                        <div className="bg-red-50 dark:bg-red-950 border border-red-200 dark:border-red-800 rounded-lg p-2">
                          <p className="text-xs font-medium text-red-800 dark:text-red-200">
                            Interactions: {supplement.interactions.join(', ')}
                          </p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {filteredSupplements.length === 0 && (
        <div className="text-center py-8">
          <Pill className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
          <p className="text-muted-foreground">
            {searchQuery ? "No supplements found matching your search." : "No supplements in your library yet."}
          </p>
          <Button className="mt-4" size="sm">
            <Plus className="w-4 h-4 mr-2" />
            Add Your First Supplement
          </Button>
        </div>
      )}
    </div>
  );
}