import dayjs from 'dayjs';
import dayjs_utc from 'dayjs/plugin/utc.js';
import dayjs_duration from 'dayjs/plugin/duration.js';
import dayjs_realtivetime from 'dayjs/plugin/relativeTime.js';

import 'dayjs/locale/en.js';
import 'dayjs/locale/es.js';

const initializeDayJs = () => {
  dayjs.extend(dayjs_utc);
  dayjs.extend(dayjs_duration);
  dayjs.extend(dayjs_realtivetime);
};

export { initializeDayJs };
