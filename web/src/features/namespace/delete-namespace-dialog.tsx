import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/ui/dialog'
import { Button } from '@/shared/ui/button'
import { Textarea } from '@/shared/ui/textarea'
import { Label } from '@/shared/ui/label'

interface DeleteNamespaceDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  namespaceName: string
  onConfirm: (reason: string) => void | Promise<void>
}

export function DeleteNamespaceDialog({
  open,
  onOpenChange,
  namespaceName,
  onConfirm,
}: DeleteNamespaceDialogProps) {
  const { t } = useTranslation()
  const [reason, setReason] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleConfirm = async () => {
    if (!reason.trim()) {
      return
    }

    setIsSubmitting(true)
    try {
      await onConfirm(reason)
      setReason('')
      onOpenChange(false)
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleOpenChange = (newOpen: boolean) => {
    if (!isSubmitting) {
      if (!newOpen) {
        setReason('')
      }
      onOpenChange(newOpen)
    }
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{t('namespace.delete.confirm.title')}</DialogTitle>
          <DialogDescription>
            {t('namespace.delete.confirm.description', { name: namespaceName })}
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="delete-reason">{t('namespace.delete.reason.label')}</Label>
            <Textarea
              id="delete-reason"
              placeholder={t('namespace.delete.reason.placeholder')}
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              disabled={isSubmitting}
              rows={3}
            />
          </div>
        </div>
        <DialogFooter className="sm:justify-center sm:space-x-3">
          <Button
            variant="outline"
            onClick={() => handleOpenChange(false)}
            disabled={isSubmitting}
          >
            {t('dialog.cancel')}
          </Button>
          <Button
            variant="destructive"
            onClick={handleConfirm}
            disabled={!reason.trim() || isSubmitting}
          >
            {t('namespace.delete.button')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
