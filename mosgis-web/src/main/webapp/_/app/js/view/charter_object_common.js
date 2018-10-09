define ([], function () {
    
    var form_name = 'charter_object_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=charter_object_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 180},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
//                            {id: 'charter_object_common_services', caption: 'Услуги'},
                            {id: 'charter_object_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_charter_object_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            

        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        
                
        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                    {name: 'startdate', type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item ['ctr.effectivedate']),
                        end:      dt_dmy (data.item ['ctr.plandatecomptetion']),
                    }},
                    {name: 'enddate',   type: 'date', options: {
                        keyboard: false,
                        start:    dt_dmy (data.item ['ctr.effectivedate']),
                        end:      dt_dmy (data.item ['ctr.plandatecomptetion']),
                    }},
                    {name: 'id_reason', type: 'list', options: {items: data.vc_charter_object_reasons.items}},
                    {name: 'ismanagedbycontract', type: 'list', options: {items: [
                        {id: 0, text: 'без договора'},
                        {id: 1, text: 'по договору управления'},
                    ]}},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})