import { Progress } from "./ui/progress";
import { Clock, CheckCircle } from "lucide-react";

interface ProgressIndicatorProps {
  completed: number;
  total: number;
}

export function ProgressIndicator({ completed, total }: ProgressIndicatorProps) {
  const percentage = total > 0 ? (completed / total) * 100 : 0;
  const remaining = total - completed;
  
  // Mock estimated time based on remaining items
  const estimatedMinutes = remaining * 2; // Assume 2 minutes per item

  return (
    <div className="bg-card border-b border-border px-4 py-3">
      <div className="space-y-2">
        {/* Progress Bar */}
        <div className="flex items-center gap-3">
          <Progress value={percentage} className="flex-1" />
          <span className="text-sm font-medium text-card-foreground whitespace-nowrap">
            {completed}/{total}
          </span>
        </div>

        {/* Status Info */}
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-1.5">
              <CheckCircle className="w-4 h-4 text-green-500" />
              <span className="text-sm text-muted-foreground">
                {completed} items collected
              </span>
            </div>
            
            {remaining > 0 && (
              <>
                <div className="h-4 w-px bg-border"></div>
                <div className="flex items-center gap-1.5">
                  <Clock className="w-4 h-4 text-blue-500" />
                  <span className="text-sm text-muted-foreground">
                    ~{estimatedMinutes}min remaining
                  </span>
                </div>
              </>
            )}
          </div>

          {percentage === 100 ? (
            <span className="text-sm font-medium text-green-600">
              🎉 Shopping Complete!
            </span>
          ) : (
            <span className="text-sm font-medium text-card-foreground">
              {percentage.toFixed(0)}% done
            </span>
          )}
        </div>
      </div>
    </div>
  );
}