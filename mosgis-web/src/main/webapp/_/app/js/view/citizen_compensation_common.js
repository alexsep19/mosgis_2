define ([], function () {
    
    var form_name = 'citizen_compensation_common_form'
    
    return function (data, view) {
        
        $_F5 = function (data) {
        
            var it = data.item

            it.__read_only = data.__read_only
            
            var r = clone (data.item)
            
            var f = w2ui [form_name]
            
            f.record = r
            
            $('div[data-block-name=citizen_compensation_common] input').prop ({disabled: data.__read_only})
            $('div[data-block-name=citizen_compensation_common] input[name=type_]').prop ({disabled: true})

            f.refresh ()
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
                            {id: 'citizen_compensation_to_categories', caption: 'Категории'},
//                            {id: 'citizen_compensation_common_', caption: 'Решение'},
                            {id: 'citizen_compensation_common_log', caption: 'История изменений'},
                        ],
                        onClick: $_DO.choose_tab_citizen_compensation_common
                    }                
                },
                
            ],
            
            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },            
            
        });
        
        var $panel = $(w2ui ['passport_layout'].el ('top'))
                
        fill (view, data.item, $panel)        

        var it = data.item

        $panel.w2reform ({ 
        
            name   : form_name,
            
            record : data.item,                
            
            fields : [                     
                    {name: 'uuid_person', type: 'list', options: {
                        url: '/_back/?type=vc_persons',
                        filter: false,
                        cacheMax: 50,
                        items: [{id: it.uuid_person, text: it['person.label']}],
                        selected: {id: it.uuid_person, text: it['person.label']},
                        onRequest: function(e) {
                            e.postData = {
                                search: [
                                    {field: 'label_uc', operator: 'contains', value: e.postData.search}
                                ],
                                searchLogic: 'OR',
                                offset: 0,
                                limit: 50
                            }
                        },
                        onLoad: function (e) {
                            e.data = {
                                status: "success", 
                                records: e.data.content.root.map (function (i) {return {
                                    id: i.id, 
                                    text: i.label + ' ' + [
                                        {label: 'СНИЛС', value: i.snils},
                                        {label: 'д.р.', value: dt_dmy(i.birthdate)}
                                    ].filter((i) => !!i.value)
                                        .map((i) => ((i.label? (i.label + ': ') : '') + i.value))
                                        .join(' ')
                                }})
                            }
                        }
                    }},
                    {name: 'fiashouseguid', type: 'list', options: {
                        url: '/_back/?type=voc_building_addresses',
                        filter: false,
                        cacheMax: 50,
                        items: [{id: it.fiashouseguid, text: it.addr}],
                        selected: {id: it.fiashouseguid, text: it.addr},
                        onLoad: function (e) {
                            e.data = {
                                status: "success", 
                                records: e.data.content.vc_buildings.map (function (i) {return {
                                    id: i.id, 
                                    text: i.postalcode + ', ' + i.label
                                }})
                            }
                        }
                    }},
                    {name: 'registrationtype', type: 'list', options: {items: data.vc_addr_reg_types.items}},
                    {name: 'apartmentnumber', type: 'text'},
                    {name: 'flatnumber', type: 'text'},
            ],

            focus: -1,
        })

        $_F5 (data)        

    }
    
})