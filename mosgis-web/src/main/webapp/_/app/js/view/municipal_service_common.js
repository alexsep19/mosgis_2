define ([], function () {
    
    var form_name = 'municipal_service_common_form'

    return function (data, view) {
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=municipal_service_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }

        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 280},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'municipal_service_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_municipal_service_common
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
                    {name: 'mainmunicipalservicename', type: 'text'},
                    {name: 'sortorder', type: 'text'},
                    {name: 'okei', type: 'list', options: {items: data.vc_okei.items}},
                    {name: 'code_vc_nsi_2', type: 'list', options: {items: data.vc_nsi_2.items}},
                    {name: 'code_vc_nsi_3', type: 'list', options: {items: data.vc_nsi_3.items}},
                    {name: 'is_general', type: 'list', options: {items: [{id:0, text:"нет"},{id:1, text:"общедомовые нужды"}]}},
                    {name: 'selfproduced', type: 'list', options: {items: [{id:0, text:"нет"},{id:1, text:"самостоятельное производство"}]}},
            ],

            focus: -1,
            
        })

        $_F5 (data)        

    }
    
})