/**
 * Bluebell form classes infrastructure.
 * <p/> 
 * Bluebell pages have the following structure.
 * 
 *  <pre>
 *     +=============================================+
 *     +=============================================+
 * +---+-------------------------------+-------------+-+
 * | T |                               |             | |
 * | r |          Master View          |Search Views |-+
 * | e |                               |             | |
 * | e +-------------------------------+-------------+-+
 * |   |_____|     |_____|_____|_____________________|
 * | V |                                             |
 * | i |                Detail Views                 |
 * | e |                                             |
 * | w |                                             |
 * +---+---------------------------------------------+
 *     | Validation View |                           |
 *     +=============================================+
 * </pre>
 */
package org.bluebell.richclient.form;
