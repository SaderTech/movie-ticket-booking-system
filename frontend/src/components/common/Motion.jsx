import { motion, useReducedMotion } from 'framer-motion'

export function PageTransition({ children, className = '' }) {
  const reduced = useReducedMotion()
  return (
    <motion.div
      className={`page-transition ${className}`.trim()}
      initial={reduced ? false : { opacity: 0, y: 12 }}
      animate={{ opacity: 1, y: 0 }}
      exit={reduced ? undefined : { opacity: 0, y: -8 }}
      transition={{ duration: reduced ? 0 : 0.32, ease: [0.22, 1, 0.36, 1] }}
    >
      {children}
    </motion.div>
  )
}

export function Reveal({ children, className = '', delay = 0, amount = 0.16, as = 'div' }) {
  const reduced = useReducedMotion()
  const Component = motion[as] || motion.div
  return (
    <Component
      className={className}
      initial={reduced ? false : { opacity: 0, y: 22 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount }}
      transition={{ duration: reduced ? 0 : 0.52, delay: reduced ? 0 : delay, ease: [0.22, 1, 0.36, 1] }}
    >
      {children}
    </Component>
  )
}

export function StaggerGroup({ children, className = '', as = 'div' }) {
  const reduced = useReducedMotion()
  const Component = motion[as] || motion.div
  return (
    <Component
      className={className}
      initial={reduced ? false : 'hidden'}
      whileInView="visible"
      viewport={{ once: true, amount: 0.08 }}
      variants={{ hidden: {}, visible: { transition: { staggerChildren: reduced ? 0 : 0.06 } } }}
    >
      {children}
    </Component>
  )
}

export function StaggerItem({ children, className = '', as = 'div' }) {
  const Component = motion[as] || motion.div
  return (
    <Component
      className={className}
      variants={{
        hidden: { opacity: 0, y: 18 },
        visible: { opacity: 1, y: 0, transition: { duration: 0.42, ease: [0.22, 1, 0.36, 1] } },
      }}
    >
      {children}
    </Component>
  )
}
