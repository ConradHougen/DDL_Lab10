#ifdef __USE_CMSIS
#include "LPC11xx.h"
#endif

#include <cr_section_macros.h>

#include "driver_config.h"
#include "target_config.h"

#include "uart.h"
#include "timer32.h"
#include "adc.h"
#include "gpio.h"

//this goes into a timer interrupt
uint8_t HEADER = 0x77;
uint8_t SETTINGS = 0x12;
uint8_t high_byte = 0x00;
uint8_t low_byte = 0x00;
int curr_volt;
extern volatile uint32_t UARTCount;
extern volatile uint32_t timer32_0_counter;
uint32_t last_count = 0;

int main(void)
{

	init_timer32(0, TIME_INTERVAL);
	/* Enable timer 0. Our interrupt handler will begin incrementing
	 * the TimeTick global each time timer 0 matches and resets.
	 */
	enable_timer32(0);
	ADCInit( ADC_CLK );
	UARTInit(UART_BAUD);





	while(1)
	{
		ADCRead( 0 );


		if(timer32_0_counter != last_count)
		{
			 //this goes into a timer interrupt
			  curr_volt = ADCValue[0]- 512;
			  high_byte = (uint8_t) ((curr_volt & 0xFF00) >> 8);
			  low_byte = (uint8_t) ((curr_volt & 0xFF));
			  //DATA FORMAT: One Byte each
			  // HEADER, SETTINGS, high_byte, low_byte, high_byte, low_byte
			  uint8_t * pointer = &HEADER;
			  LPC_UART->IER = IER_THRE | IER_RLS;
			  UARTSend( pointer, sizeof(*pointer));
			  LPC_UART->IER = IER_THRE | IER_RLS | IER_RBR;	/* Re-enable RBR */
			  UARTCount = 0;
			  LPC_UART->IER = IER_THRE | IER_RLS;
			  UARTSend( &SETTINGS, sizeof(uint8_t));
			  LPC_UART->IER = IER_THRE | IER_RLS | IER_RBR;	/* Re-enable RBR */
			  UARTCount = 0;
			  			  LPC_UART->IER = IER_THRE | IER_RLS;
			  UARTSend( &high_byte, sizeof(uint8_t));
			  LPC_UART->IER = IER_THRE | IER_RLS | IER_RBR;	/* Re-enable RBR */
			  UARTCount = 0;
			  			  LPC_UART->IER = IER_THRE | IER_RLS;
			  UARTSend( &low_byte, sizeof(uint8_t));
			  LPC_UART->IER = IER_THRE | IER_RLS | IER_RBR;	/* Re-enable RBR */
			  UARTCount = 0;
			  			  LPC_UART->IER = IER_THRE | IER_RLS;
			  UARTSend( &high_byte, sizeof(uint8_t));
			  LPC_UART->IER = IER_THRE | IER_RLS | IER_RBR;	/* Re-enable RBR */
			  UARTCount = 0;
			  LPC_UART->IER = IER_THRE | IER_RLS;
			  UARTSend( &low_byte, sizeof(uint8_t));
			  LPC_UART->IER = IER_THRE | IER_RLS | IER_RBR;	/* Re-enable RBR */

			  last_count = timer32_0_counter;

		}
	}
}
