define ([], function () {
    
    var form_name = 'account_common_form'

    return function (data, view) {
    
        var it = data.item              
    
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
                
                {type: 'top', size: 250},
                {type: 'main', size: 400, 
                    tabs: {
                        tabs:    [
                            {id: 'account_common_items', caption: 'Помещения'},
                            {id: 'account_common_individual_services', caption: 'Индивидуальные услуги'},
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
            
            record : it,                
            
            fields : [            
            
                {name: 'label_org_customer', type: 'text'},
                {name: 'uuid_org_customer', type: 'hidden'},
            
                {name: 'isaccountsdivided', type: 'list', options: {items: [
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

                {name: 'uuid_person_customer', type: 'list', options: 
                    {
                        items: it.persons,
                        url: '/_back/?type=vc_persons',
                        postData: {
                            uuid_org: it.uuid_org, 
                            searchLogic: 'OR',
                            offset: 0,
                            limit: 50,
                        },
                        cacheMax: 50,
                        filter: false,

                        onSearch: function (e) {
                            this.options.postData['search'] = [{'value': e.search}]
                        },

                        onLoad: function (e) {
                            dia2w2ui (e)
                            e.xhr.responseJSON = JSON.parse (e.xhr.responseText)
                            e.data = e.xhr.responseJSON                                
                            $.each (e.data.records, function () {this.text = this.label})
                        }
                        
                    }
                },                    

            ],

            focus: -1,
            
            onRefresh: function (e) {e.done (function () {
            
                clickOff ($('#label_reason'))
                clickOn ($('#label_reason'), function () {openTab (it.url_reason)})
                
                clickOff ($('#label_org_customer'))
                clickOn ($('#label_org_customer'), $_DO.open_orgs_account_common)
                
            })}                
            
        })

        $_F5 (data)        

    }
    
})