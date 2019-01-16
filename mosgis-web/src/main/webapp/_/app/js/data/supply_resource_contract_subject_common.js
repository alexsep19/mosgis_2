define ([], function () {

    var form_name = 'supply_resource_contract_subject_common_form'

    $_DO.cancel_supply_resource_contract_subject_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'supply_resource_contract_subjects'}, {}, function (data) {

            data.__read_only = true

            var it = data.item

            $_F5 (data)

        })

    }

    $_DO.edit_supply_resource_contract_subject_common = function (e) {

        var data = {item: w2ui [form_name].record}

//        if (data.item.id_status == 10) die ('foo', 'В настоящий момент данная запись передаётся в ГИС ЖКХ. Операция отменена.')

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.update_supply_resource_contract_subject_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (!v.code_vc_nsi_3) die ('code_vc_nsi_3', 'Вы забыли указать вид коммунальной услуги')
        if (!v.code_vc_nsi_239) die('code_vc_nsi_239', 'Вы забыли указать коммунальный ресурс')

        if (!v.startsupplydate)
            die('startsupplydate', 'Укажите, пожалуйста, дату начала поставки ресурса')

        query ({type: 'supply_resource_contract_subjects', action: 'update'}, {data: v}, reload_page)

    }

    $_DO.delete_supply_resource_contract_subject_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'supply_resource_contract_subjects', action: 'delete'}, {}, reload_page)
    }

    $_DO.choose_tab_supply_resource_contract_subject_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('supply_resource_contract_subject_common.active_tab', name)

        use.block (name)

    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('supply_resource_contract_subject_common.active_tab') || 'supply_resource_contract_subject_common_log'

        data.__read_only = 1

        done (data)

    }

})