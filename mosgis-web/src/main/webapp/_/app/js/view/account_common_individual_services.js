define ([], function () {

    var grid_name = 'account_common_individual_services_grid'

    function recalcToolbar (e) {e.done (function () {
    
        var g = w2ui [grid_name]

        var t = g.toolbar
        var r = g.get (g.getSelection () [0])

        var id_status = r ? r.id_ctr_status : -1

        switch (id_status) {        
            case 10:
            case 11:
                t.enable ('approve')
                break
            default:
                t.disable ('approve')                
        }
        
        switch (id_status) {        
            case 10:
            case 11:
            case 14:
            case 34:
            case 40:
                t.enable ('delete')
                break
            default:
                t.disable ('delete')
        }        
        
        switch (id_status) {        
            case 10:
            case 11:
            case 14:
            case 104:
                t.enable ('edit')
                break
            default:
                t.disable ('edit')
        }        
        
    })}

    return function (data, view) {

        data = $('body').data ('data')

        var it = data.item

        var can_edit = it._can.edit || it._can.alter

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: grid_name,

            multiSelect: false,

            show: {
                toolbar: true,
                toolbarInput: false,
                toolbarAdd: can_edit,
//                toolbarEdit: can_edit,
//                toolbarDelete: can_edit,
                footer: true,
            },     
            
            
            toolbar: {

                items: !can_edit ? null : [
                    {type: 'button', id: 'edit', caption: 'Изменить', onClick: $_DO.edit_account_common_individual_services, disabled: true, icon: 'w2ui-icon-pencil'},
                    {type: 'button', id: 'delete', caption: 'Удалить', onClick: $_DO.delete_account_common_individual_services, disabled: true, icon: 'w2ui-icon-cross'},
                    {type: 'button', id: 'approve', caption: 'Разместить', onClick: $_DO.approve_account_common_individual_services, disabled: true, off: $_USER.role.admin || it.id_ctr_status != 40},
                ].filter (not_off),

                onRefresh: function (e) {

                    if (e.target != 'delete') return

                    var g = this.owner
                    var t = g.toolbar
                    var r = g.get (g.getSelection () [0])
                    var id_status = r ? r.id_ctr_status : -1
                    var b = e.item

                    if (id_status == 40 || id_status == 34 || id_status == 11) {
                        b.caption = 'Аннулировать'
                        b.onClick = $_DO.annul_account_common_individual_services
                    }
                    else {
                        b.caption = 'Удалить'
                        b.onClick = $_DO.delete_account_common_individual_services
                    }

                    b.text = b.caption

                }

            },             

            columns: [                
                {field: 'svc.label', caption: 'Услуга', size: 50},
                {field: 'begindate', caption: 'Начало', size: 18, render: _dt},
                {field: 'enddate', caption: 'Окончание', size: 18, render: _dt},
                {field: 'label', caption: 'Основание', size: 30, attr: 'data-ref=1'},
                {field: 'id_ctr_status', caption: 'Статус', size: 100, voc: data.vc_gis_status},
                {field: 'soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'soap.err_text', caption: 'Ошибка',    size: 30},
            ],

            postData: {data: {uuid_account: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=account_individual_services',

            onSelect: !can_edit ? null : recalcToolbar,
            onUnselect: !can_edit ? null : recalcToolbar,

            onAdd: $_DO.create_account_common_individual_services,
//            onEdit: $_DO.edit_account_common_individual_services,
            onDblClick: $_DO.edit_account_common_individual_services,
//            onDelete: $_DO.delete_account_common_individual_services,

            onClick: function (e) {                
            
                var r = this.get (e.recid)
            
                switch (this.columns [e.column].field) {
                    case 'label': return $_DO.download_account_common_individual_services (e)
                    case 'soap.ts':    if (r ['soap.ts']) return openTab ('/out_soap_rq/' + r.id_log)
                    case 'soap.ts_rp': if (r ['soap.ts_rp']) return openTab ('/out_soap_rp/' + r ['soap.uuid_ack'])
                }                

            }

        }).refresh ();

    }

})