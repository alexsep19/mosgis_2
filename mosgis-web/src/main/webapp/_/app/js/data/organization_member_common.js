define ([], function () {

    var form_name = 'organization_member_common_form'

    $_DO.cancel_organization_member_common = function (e) {

        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record

        query ({type: 'organization_members'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }

    $_DO.edit_organization_member_common = function (e) {

        var data = {item: w2ui [form_name].record}

        data.item.participant = data.item.participant.id

        data.__read_only = false

        var $form = w2ui [form_name]

        $_F5 (data)

    }

    $_DO.update_organization_member_common = function (e) {

        if (!confirm ('Сохранить изменения?')) return

        var f = w2ui [form_name]

        var v = f.values ()

        if (v.dt_from_participant && v.dt_to_participant && v.dt_from_participant > v.dt_to_participant)
            die ('dt_to_participant', 'Окончание периода избрания не может предшествовать началу периода избрания')

        if (v.dt_from_chairman && v.dt_to_chairman && v.dt_from_chairman > v.dt_to_chairman)
            die('dt_to_chairman', 'Окончание периода правления не может предшествовать началу периода правления')

        if (v.dt_from && v.dt_to && v.dt_from > v.dt_to)
            die('dt_to', 'Дата исключения из членов товарищества, кооператива должна быть позже даты принятия в члены товарищества, кооператива')

        upload_file_from_organization_member_common(v, function(){
            query({type: 'organization_members', action: 'update'}, {data: v}, reload_page)
        })
    }

    function upload_file_from_organization_member_common(v, onfinish) {

        if (!v['files']) {
            return onfinish();
        }
        var file = get_valid_gis_file(v, 'files')

        Base64file.upload(file, {

            type: 'organization_member_files',

            data: {
                uuid_org_member: $_REQUEST.id
            },

//            onprogress: show_popup_progress(file.size),

            onloadend: function (id) {
                var form = w2ui [form_name]
                form.get('files').options.items = [{id: id, text: file.name}]
                form.record ['uuid_file_from'] = id
                form.refresh()
                onfinish ();
            }
        })
    }

    $_DO.delete_organization_member_common = function (e) {
        if (!confirm ('Удалить эту запись, Вы уверены?')) return
        query ({type: 'organization_members', action: 'delete'}, {}, reload_page)
    }

    $_DO.undelete_organization_member_common = function (e) {
        if (!confirm ('Восстановить эту запись, Вы уверены?')) return
        query ({type: 'organization_members', action: 'undelete'}, {}, reload_page)
    }

    $_DO.choose_tab_organization_member_common = function (e) {

        var name = e.tab.id

        var layout = w2ui ['passport_layout']

        if (layout) {
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        localStorage.setItem ('organization_member_common.active_tab', name)

        use.block (name)

    }

    $_DO.download_file_from_organization_member_common = function (e) {

        var box = $('body')

        function label(cur, max) {
            return String(Math.round(100 * cur / max)) + '%'
        }

        var data = clone($('body').data('data'))

        var id = data.files && data.files[0]? data.files[0].id : undefined

        w2utils.lock(box, label(0, 1))

        download({
            type: 'organization_member_files',
            id: id,
            action: 'download',
        }, {}, {

            onprogress: function (cur, max) {
                $('.w2ui-lock-msg').html('<br><br>' + label(cur, max))
            },

            onload: function () {
                w2utils.unlock(box)
            },
        })
    }

    return function (done) {

        w2ui ['topmost_layout'].unlock ('main')

        var data = clone ($('body').data ('data'))

        data.active_tab = localStorage.getItem ('organization_member_common.active_tab') || 'organization_member_common_log'

        data.__read_only = 1

        var it = data.item

        $.each(data.files, function() {this.text = this.label})

        it.file_from = data.files? data.files[0] : undefined

        done(data)

    }

})