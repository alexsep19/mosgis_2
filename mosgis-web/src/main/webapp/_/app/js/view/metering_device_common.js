define ([], function () {
    
    var form_name = 'metering_device_common_form'

    return function (data, view) {
    
        var it = data.item              
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=metering_device_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 250},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'metering_device_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_metering_device_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        
        
        var now = dt_dmy (new Date ().toJSON ())

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : it,                
            
            fields : [            
            
                    {name: 'meteringdevicenumber', type: 'text'},
                    {name: 'meteringdevicestamp', type: 'text'},
                    {name: 'meteringdevicemodel', type: 'text'},
                    {name: 'installationdate', type: 'date', options: {end: now}},
                    {name: 'commissioningdate', type: 'date', options: {end: now}},
            
/*            
                {name: 'label_org_customer', type: 'text'},
                {name: 'uuid_org_customer', type: 'hidden'},
            
                {name: 'ismetering_devicesdivided', type: 'list', options: {items: [
                    {id: -1, text: '[нет данных]'},
                    {id:  0, text: 'нет, не разделен(ы)'},
                    {id:  1, text: 'да, разделен(ы)'},
                ]}},
                
                {name: 'isrenter', type: 'list', options: {items: [
                    {id: -1, text: '[нет данных]'},
                    {id:  0, text: 'нет, не является нанимателем'},
                    {id:  1, text: 'да, является нанимателем'},
                ]}},
                
                {name: 'totalsquare', type: 'float', options: {min: 0, precision: 2}},
                {name: 'residentialsquare', type: 'float', options: {min: 0, precision: 2}},
                {name: 'heatedarea', type: 'float', options: {min: 0, precision: 2}},
            
                {name: 'livingpersonsnumber', type: 'int', options: {min: 0, max: 9999}},
*/                    

            ],

            focus: -1,
/*            
            onRefresh: function (e) {e.done (function () {
            
                clickOff ($('#label_reason'))
                clickOn ($('#label_reason'), function () {openTab (it.url_reason)})                
                
            })}                
*/            
        })

        $_F5 (data)        

    }
    
})