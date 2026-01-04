interface AlertsCreateParams {
  name: string
  condition: any
  enabled?: boolean
}

export async function alertsCreate(params: AlertsCreateParams, correlationId?: string) {
  const { name, condition, enabled = true } = params

  if (!name) {
    throw new Error('name is required')
  }

  if (!condition) {
    throw new Error('condition is required')
  }

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'system-mcp',
      eventType: 'alert_create_start',
      message: 'Creating alert rule',
      correlationId,
      data: { name, enabled },
    })
  )

  const alertRule = {
    id: Math.floor(Math.random() * 10000),
    name,
    condition,
    enabled,
    createdAt: new Date().toISOString(),
  }

  console.log(
    JSON.stringify({
      timestamp: new Date().toISOString(),
      level: 'INFO',
      service: 'system-mcp',
      eventType: 'alert_create_success',
      message: 'Alert rule created',
      correlationId,
      data: { alertId: alertRule.id, name },
    })
  )

  return {
    alert: alertRule,
    timestamp: new Date().toISOString(),
  }
}
