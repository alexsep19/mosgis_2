define ([], function () {
    
    var form_name = 'interval_common_form'

    return function (data, view) {
    
        var it = data.item              
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=interval_common] input').prop ({disabled: data.__read_only})

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
                            {id: 'interval_common_log', caption: 'История изменений'}
                        ],
                        onClick: $_DO.choose_tab_interval_common
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
            
            record : it,                
            
            fields : [            
                {name: 'code_vc_nsi_3', type: 'list', options: {items: data.vc_nsi_3.items}},
                {name: 'code_vc_nsi_239', type: 'list', options: {items: data.vc_nsi_239.items}},

                {name: 'startdateandtime', type: 'datetime'},
                {name: 'enddateandtime', type: 'datetime'},

                {name: 'intervalreason', type: 'text'}
            ],

            focus: -1,

            onChange: function (e) {

                if (e.target == "code_vc_nsi_3") {

                    var form = this

                    delete form.record.code_vc_nsi_239

                    var code_vc_nsi_3 = e.value_new.id

                    e.done(function () {
                        var f_resource = form.get('code_vc_nsi_239').options
                        f_resource.items = data.service2resource[code_vc_nsi_3]
                        if (f_resource.items.length == 1) {
                            f_resource.selected = f_resource.items[0]
                        }
                        form.refresh()
                    })
                }
            }
        })

        $_F5 (data)        

    }
    
})