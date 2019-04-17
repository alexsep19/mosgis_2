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
                            html: '&nbsp;&nbsp;&nbsp;С:',
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
                            html: '&nbsp;&nbsp;&nbsp;по:',
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

                        if (e.item.type == 'menu') {
                            var s = e.subItem
                            if (s) e.item.text = s.text
                            e.done (function () {
                                thos.owner.reload ()
                            })
                        }
                        
                    }
                    
                },
                
                searchData: [
                    {field: 'uuid_account', value: [data.accounts [0]], operator: 'in'}
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
                ],
                
                postData: {data: {uuid_org: $_USER.uuid_org}},

                url: '/_back/?type=payment_documents',
                            
            }).refresh ()

       })

    }

})