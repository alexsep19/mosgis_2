define ([], function () {
    
    var form_name = 'account_common_form'

    return function (data, view) {
    
        var it = data.item
        
        it.status_label = data.vc_gis_status [it.id_ctr_status]            
        it.type_label = data.vc_acc_types [it.id_type]
        it.org_label = it ['org.label']
        
        if (it.uuid_contract) {
            it.label_reason = 'Договор управления №' + it ['ca.docnum'] + ' от '  + dt_dmy (it ['ca.signingdate'])
            it.url_reason = '/mgmt_contract/' + it.uuid_contract
        }       
    
        $_F5 = function (data) {
        
            data.item.__read_only = data.__read_only
            
            var r = clone (data.item)

            w2ui [form_name].record = r
            
            $('div[data-block-name=account_common] input').prop ({disabled: data.__read_only})

            w2ui [form_name].refresh ()

        }
    
        var layout = w2ui ['topmost_layout']

        var $panel = $(layout.el ('main'))

        $panel.w2relayout ({
        
            name: 'passport_layout',
            
            panels: [
                
                {type: 'top', size: 160},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'account_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_account_common
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
/*            
                {name: 'additionalservicetypename', type: 'text'},
                {name: 'okei', type: 'list', options: {items: data.vc_okei.items}},
*/                
            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (function () {
                clickOff ($('#label_reason'))
                clickOn ($('#label_reason'), function () {openTab (it.url_reason)})
            })}                
            
        })

        $_F5 (data)        

    }
    
})