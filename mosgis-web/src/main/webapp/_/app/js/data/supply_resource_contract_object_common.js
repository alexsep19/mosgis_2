define ([], function () {

    var form_name = 'supply_resource_contract_object_common_form'

    $_DO.cancel_supply_resource_contract_object_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'supply_resource_contract_objects'}, {}, function (data) {

            data.__read_only = true

            var it = data.item

            $_F5 (data)

        })

    }

    $_DO.edit_supply_resource_contract_object_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.update_supply_resource_contract_object_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.fiashouseguid) die ('fiashouseguid', 'Вы забыли указать адрес')

        query ({type: 'supply_resource_contract_objects', action: 'update'}, {data: v}, reload_page)

    }

    $_DO.delete_supply_resource_contract_object_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'supply_resource_contract_objects', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_supply_resource_contract_object_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('supply_resource_contract_object_common.active_tab', name)

        use.block (name)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        var it = data.item

        it.status_label = data.vc_gis_status [it.id_ctr_status]

        data.active_tab = localStorage.getItem ('supply_resource_contract_object_common.active_tab') || 'supply_resource_contract_object_common_log'

        data.__read_only = 1

        done (data)

    }

})