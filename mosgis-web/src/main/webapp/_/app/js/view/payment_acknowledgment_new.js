define ([], function () {

    return function (data, view) {

        $(fill (view, {})).w2uppop ({}, function () {

            $('#w2ui-popup .w2ui-form').w2reform ({

                name: 'org_work_form',

                record: {},

                fields : [                                
                ],

            })
            
            $('#payment_documents_container').w2regrid ({ 
            
                name: 'new_objects_grid',
                
                multiSelect: 0,
                
                show: {
                    toolbar: 1,
                    toolbarInput: 0,
                    footer: 0,
                    columnHeaders: 1,
                    selectColumn: 1
                },     
                
                toolbar: {
                
                    items: [                        
                        {
                            type: 'html',
                            html: '&nbsp;&nbsp;&nbsp;Периоды:',
                        },                        
                        {
                            type: 'menu',
                            id: 'period_from',
                            text: data.periods [35].text,
                            items: data.periods,
                            selected: data.periods [35].id,
                        },
                        {
                            type: 'html',
                            html: '&nbsp;&ndash;&nbsp;',
                        },                        
                        {
                            type: 'menu',
                            id: 'period_to',
                            text: data.periods [0].text,
                            items: data.periods,
                            selected: data.periods [0].id,                            
                        },
                    ],
                    
                    onClick: function (e) {

                        if (e.item.type != 'menu') return
                        
                        var ni = e.target.split (':')
                        
                        if (ni.length < 2) return
                        
                        var ndx = (function () {switch (ni [0]) {
                            case 'period_from': return 0
                            case 'period_to':   return 1
                        }}) ()
                       
                        var s = e.subItem; if (!s) return
                        e.item.text = s.text
                        e.item.selected = s.id
                        var grid = this.owner

                        e.done (function () {
                            grid.reload ()
                        })

                    }
                    
                },
                
                onRequest: function (e) {
                
                    var tb = this.toolbar
                    
                    var pd = e.postData; if (!pd.search) pd.search = []
                    
                    var s = pd.search.filter (function (i) {return i.field == 'dt_period'}) [0]

                    if (!s) pd.search.push (s = {field: 'dt_period', operator: 'between'})

                    s.value = [
                        tb.get ('period_from').selected, 
                        tb.get ('period_to').selected, 
                    ]
                    
                    pd.searchLogic = "AND"

                },
                
                searchData: [
                    {field: 'uuid_account', value: [data.accounts [0]], operator: 'in'},
                ],
                
                onRefresh: function (e) {
                    this.last.logic = 'AND'
                },
                
                searches: [
                    {field: 'uuid_account', caption: 'Лицевой счёт', type: 'enum', options: {items: data.accounts}},
                    {field: 'id_ctr_status', caption: 'Статус',      type: 'enum', options: {items: data.vc_pay_doc_types.items}},
                
    //                {field: 'dt_period', caption: 'Период',         type: 'date', operator: 'between', operators: ['between']},
    //                {field: 'id_ctr_status', caption: 'Статус',     type: 'enum', options: {items: data.vc_gis_status.items}},
    //                {field: 'is_deleted', caption: 'Статус записи', type: 'enum', options: {items: [
    //                    {id: "0", text: "Актуальные"},
    //                    {id: "1", text: "Удалённые"},
    //                ]}},
                ].filter (not_off),

                columns: [                
                    {field: 'acct.accountnumber', caption: '№ ЛС', size: 20},
                    {field: 'paymentdocumentnumber', caption: '№ ПД', size: 20},
                    {field: 'id_type', caption: 'Тип', size: 10, voc: data.vc_pay_doc_types},
                    {field: 'dt_period', caption: 'Период', size: 22, render: function (r) {
                        return w2utils.settings.fullmonths [r.month - 1] + ' ' + r.year
                    }},
                    {field: 'totalpayablebypdwith_da', caption: 'Сумма', size: 20, render: 'money'},
                ],
                
                postData: {data: {uuid_org: $_USER.uuid_org}},

                url: '/_back/?type=payment_documents',
                            
            }).refresh ()

       })

    }

})