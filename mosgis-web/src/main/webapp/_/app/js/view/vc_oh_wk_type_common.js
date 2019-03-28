define ([], function () {
    
    var form_name = 'vc_oh_wk_type_common_form'

    return function (data, view) {

        $_F5 = function (data) {

            data.item.__read_only = data.__read_only

            var r = clone (data.item)

            var f = w2ui [form_name]

            f.record = r

            $('div[data-block-name=vc_oh_wk_type_common] input').prop ({disabled: data.__read_only})

            f.refresh ()

        }
    
        var it = data.item              
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 260},
                {type: 'main', size: 400,
                    tabs: {
                        tabs:    [
                            {id: 'vc_oh_wk_type_common_log', caption: 'История изменений'},
                        ].filter (not_off),
                        onClick: $_DO.choose_tab_vc_oh_wk_type_common
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
                {name: 'servicename', type: 'text'},
                {name: 'code_vc_nsi_218', type: 'list', options: {items: data.vc_nsi_218.items}},
            ],

            focus: -1,

        })

        $_F5 (data)

    }
    
})